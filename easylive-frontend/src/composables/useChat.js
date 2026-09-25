/**
 * 私信聊天状态（模块级单例，同 useAuth 模式）
 *
 * 职责：
 * - 抽屉开关（drawerVisible）与"当前跟谁聊"（activePeer）
 * - 我的关注名单（follows）= 私信面板的联系人
 * - STOMP over SockJS 连接生命周期（打开才连、关闭即断，断开期间的消息靠进会话时拉历史补上）
 * - 按对端分组的消息 + 未读数 + 历史记录（openPeer 时以接口结果重建）
 *
 * 通信约定（和后端对好）：
 * - 连接地址：/api/ws?token=Bearer <jwt>（同源走 vite proxy，身份由后端握手拦截器从 query 读）
 * - 收：订阅 /user/queue/chat，服务器只往"发给我的"那条推送
 * - 发：publish 到 /app/chat，body = { aimUser, content }（fromUserId 由后端用登录身份填，前端不传）
 * - 服务器不回显给发送者，所以自己发的消息本地"乐观追加"
 */
import { computed, reactive, ref, watch } from 'vue'
import { Client } from '@stomp/stompjs'
import SockJS from 'sockjs-client'
import { useAuth } from '@/composables/useAuth.js'
import { getFollowList } from '@/api/follow.js'
import { getChatRecords } from '@/api/chat.js'

const { isLoggedIn, myUserId, userName, showLogin } = useAuth()

const drawerVisible = ref(false)
const activePeer = ref(null) // { userId, nickName, avatar } | null -> 名单视图
const follows = ref([]) // 我关注的人（联系人名单）
const messagesByPeer = reactive({}) // peerId -> [{ fromUserId, fromNickName, content, mine, time }]
const unreadByPeer = reactive({}) // peerId -> 未读数（仅当前打开抽屉、且没在看 TA 的会话时累计）
const loadingPeers = reactive({}) // peerId -> 是否正在拉历史记录（UI 显示"加载中"，避免空态闪一下）
const connected = ref(false)

let client = null

/** 某个对端的消息数组（供组件渲染；没有则空数组） */
function messagesOf(peerId) {
  return messagesByPeer[peerId] || []
}

/** 某个对端是否正在拉历史（供模板区分"加载中"和"真没聊过"） */
function loadingOf(peerId) {
  return !!loadingPeers[peerId]
}

function wsUrl() {
  const token = localStorage.getItem('easylive-token')
  // 后端 WebSocketInterceptor 兼容裸 JWT / "Bearer " 前缀，这里按约定带前缀
  return `/api/ws?token=${encodeURIComponent('Bearer ' + token)}`
}

/** 收消息：按 fromUserId 归组，mine 判断靠"我自己的 userId" */
function handleIncoming(msg) {
  const peerId = msg.fromUserId
  if (!peerId) return
  if (!messagesByPeer[peerId]) messagesByPeer[peerId] = []
  const mine = msg.fromUserId === myUserId.value
  messagesByPeer[peerId].push({
    fromUserId: msg.fromUserId,
    fromNickName: msg.fromNickName,
    content: msg.content,
    mine,
    time: Date.now()
  })
  // 没在看这个对端才累计未读
  if (!mine && activePeer.value?.userId !== peerId) {
    unreadByPeer[peerId] = (unreadByPeer[peerId] || 0) + 1
  }
}

/** 建立连接（幂等：已存在 client 就只 activate） */
function ensureConnected() {
  if (!isLoggedIn.value || !myUserId.value) return
  if (client) {
    client.activate()
    return
  }
  const token = localStorage.getItem('easylive-token')
  if (!token) return

  client = new Client({
    // 只能用 webSocketFactory（配了 brokerURL 就不能再用 SockJS）；每次重连工厂会被重调，给全新 SockJS
    webSocketFactory: () => new SockJS(wsUrl()),
    reconnectDelay: 3000,
    heartbeatIncoming: 10000,
    heartbeatOutgoing: 10000,
    debug: () => {}, // 关掉控制台 STOMP 帧日志
    onConnect: () => {
      connected.value = true
      // 注意：连接重建后订阅会失效，必须在每次 onConnect 里重新订阅
      client.subscribe('/user/queue/chat', (frame) => {
        let msg
        try {
          msg = JSON.parse(frame.body) // frame.body 是 JSON 字符串，必须 parse
        } catch {
          return
        }
        handleIncoming(msg)
      })
    },
    onWebSocketClose: () => {
      connected.value = false
    },
    onStompError: (frame) => {
      connected.value = false
      console.error('私信连接出错', frame.headers?.message)
    }
  })
  client.activate()
}

/** 断开并停掉自动重连（不调 deactivate 的话 reconnectDelay 会一直重试，造成连接泄漏） */
function disconnect() {
  client?.deactivate()
  client = null
  connected.value = false
}

/** 打开抽屉：未登录先弹登录框，登录了再连并刷新名单 */
function openDrawer() {
  if (!isLoggedIn.value || !myUserId.value) {
    showLogin()
    return
  }
  drawerVisible.value = true
  refreshFollows()
  ensureConnected()
}

/** 关闭抽屉：回到名单下次再连（本期纯实时，关掉就不收后台消息） */
function closeDrawer() {
  drawerVisible.value = false
  activePeer.value = null
  disconnect()
}

/**
 * 从名单进入某个对端的单聊。
 * 每次进入都拉一次历史，用接口结果整体重建该会话的消息流：
 * - 历史记录能补上"之前没连着时对方发的消息"，也能自动去重（重开不会和旧内存重复）；
 * - 后端是倒序返回（新在前），这里 reverse 成时间从旧到新，正好从顶部铺到输入框；
 * - 拉取在途时新收到的实时/自己新发的消息（本地 time 晚于发起时刻）不会被历史覆盖，
 *   补到尾部兜住。
 */
async function openPeer(peer) {
  activePeer.value = peer
  unreadByPeer[peer.userId] = 0
  if (!isLoggedIn.value || !myUserId.value) return

  const peerId = peer.userId
  loadingPeers[peerId] = true
  const since = Date.now() // 记住发起时刻，用于判断"请求飞行期间新到的"本地消息
  try {
    const records = await getChatRecords(peerId) // 后端倒序：最新在数组最前
    const history = (records || [])
      .map((r) => ({
        id: r.id, // 后端自增主键：消息唯一指纹，去重靠它
        fromUserId: r.messageFrom,
        fromNickName: null, // 历史记录后端没带昵称；界面的昵称/头像来自联系人名单，气泡上不显示
        content: r.messageContent,
        mine: r.messageFrom === myUserId.value,
        time: r.time // 后端 ISO 字符串，formatTime 可直接 new Date 解析
      }))
      .reverse() // 翻成时间从旧到新
    const historyIds = new Set(history.map((m) => m.id))
    // 本地已有消息：有 id 且已入库的以历史为准丢掉（id 相同 = 同一条，防重复）；
    // 没 id 的（实时/刚发、还在路上）只保留请求发起后新到的，避免被历史覆盖丢一条
    const localKeep = (messagesByPeer[peerId] || [])
      .filter((m) => (m.id != null ? !historyIds.has(m.id) : (m.time || 0) >= since))
    messagesByPeer[peerId] = [...history, ...localKeep]
  } catch {
    // 历史拉失败不阻塞进入会话：保留当前内存里的消息，实时继续补
  } finally {
    loadingPeers[peerId] = false
  }
}

/** 返回名单 */
function backToList() {
  activePeer.value = null
  // 顺手重拉一次联系人名单：对方昵称/头像在别处改了，回到名单就能看到新的，
  // 也避免"一直开着抽屉名字不变"的旧印象
  refreshFollows()
}

/** 刷新联系人名单 = 我关注的人 */
async function refreshFollows() {
  if (!isLoggedIn.value) return
  try {
    follows.value = await getFollowList()
  } catch {
    // 名单拉失败不阻塞抽屉，保持上一次内容
  }
}

/** 发消息；服务端不回显给自己，这里乐观追加一条右侧气泡 */
function send(text) {
  const content = (text || '').trim()
  const peer = activePeer.value
  if (!content || !peer) return false
  if (!client?.connected) return false

  client.publish({
    destination: '/app/chat',
    body: JSON.stringify({ aimUser: peer.userId, content })
  })

  if (!messagesByPeer[peer.userId]) messagesByPeer[peer.userId] = []
  messagesByPeer[peer.userId].push({
    fromUserId: myUserId.value,
    fromNickName: userName.value,
    content,
    mine: true,
    time: Date.now()
  })
  return true
}

/** 登出时整体复位：断开 + 清空会话与未读 */
function resetAll() {
  disconnect()
  drawerVisible.value = false
  activePeer.value = null
  follows.value = []
  Object.keys(messagesByPeer).forEach((k) => delete messagesByPeer[k])
  Object.keys(unreadByPeer).forEach((k) => delete unreadByPeer[k])
  Object.keys(loadingPeers).forEach((k) => delete loadingPeers[k])
}
watch(isLoggedIn, (val) => {
  if (!val) resetAll()
})

export function useChat() {
  return {
    drawerVisible,
    activePeer,
    follows,
    connected,
    unreadByPeer,
    messagesOf,
    loadingOf,
    openDrawer,
    closeDrawer,
    openPeer,
    backToList,
    refreshFollows,
    send
  }
}
