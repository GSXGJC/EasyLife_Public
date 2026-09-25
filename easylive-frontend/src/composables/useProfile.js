/**
 * 「我的」页面数据
 *
 * - profile / updateProfile：资料编辑字段（头像/性别/生日/学校/简介，不含昵称）。
 *   后端"资料 GET/PUT"接口还没做，仍存 localStorage（mock，按账号隔离），将来做资料接口时替换。
 *   昵称不在此列：昵称只认登录账号（user.name，来自登录接口/数据库），由「我的」页直接展示。
 * - myVideos（我的投稿）+ likedVideos/collectedVideos/coinedVideos（三个互动读侧）：
 *   四个 tab 都已接真实接口，由 loadProfileVideos() 一次性并发拉取。
 */
import { ref, watch } from 'vue'
import { getMyVideos } from '@/api/video.js'
import { getLikedVideos, getCollectedVideos, getCoinedVideos } from '@/api/interaction.js'
import { useAuth } from '@/composables/useAuth.js'

// 本地资料按账号隔离，key = easylive-profile:<userId>。
// 原因：资料还没做后端接口，只能存本地。若不区分账号，A 号编辑过的
// 昵称/头像会串到 B 号头上（整台浏览器共用一份），且登出也不清，
// 于是"换账号登录还显示上一个号的名字"。
const { myUserId } = useAuth()

const STORAGE_PREFIX = 'easylive-profile'

/** 当前登录账号自己的存储 key；未登录为空串（不读也不写） */
function storageKey() {
  return myUserId.value ? `${STORAGE_PREFIX}:${myUserId.value}` : ''
}

/** 读当前账号在本地存过的资料，读到坏的/没有就返回空对象 */
function load() {
  const key = storageKey()
  if (!key) return {}
  try {
    const raw = localStorage.getItem(key)
    if (raw) return JSON.parse(raw)
  } catch {
    // 忽略损坏的本地数据
  }
  return {}
}

const profile = ref(load())

// 登录/切换账号/登出都会让 myUserId 变化 → 重载成"当前账号自己的"本地资料，
// 保证上一个号的编辑结果不会残留到下一个号（登出时 myUserId 变空串 → 清空）。
watch(myUserId, () => {
  profile.value = load()
})

// —— 四个视频列表：初始为空数组，登录后由 ProfileView 调 loadProfileVideos() 拉取 ——
const myVideos = ref([])
const likedVideos = ref([])
const collectedVideos = ref([])
const coinedVideos = ref([])
const loading = ref(false)
/** 是否加载失败（后端没启动等非登录错误）：失败和"真没有"要分开展示 */
const loadFailed = ref(false)

/**
 * 一次性拉取四个 tab 的数据：我的投稿 + 点赞/收藏/投币过的。
 * 四个接口无依赖，Promise.all 并发请求，省时间。
 * 失败分两种情况：
 * - 登录失效（code 401/1004/1005）：http.js 已统一登出+弹登录框，这里只管清空。
 * - 其他错误（后端没启动等）：标 loadFailed，页面显示"加载失败"并给重试按钮，
 *   不要误导用户以为"真没有数据"。
 */
async function loadProfileVideos() {
  loading.value = true
  loadFailed.value = false
  try {
    const [mine, liked, collected, coined] = await Promise.all([
      getMyVideos(),
      getLikedVideos(),
      getCollectedVideos(),
      getCoinedVideos()
    ])
    myVideos.value = mine
    likedVideos.value = liked
    collectedVideos.value = collected
    coinedVideos.value = coined
  } catch (e) {
    myVideos.value = []
    likedVideos.value = []
    collectedVideos.value = []
    coinedVideos.value = []
    // 登录失效走全局登出流程（http.js），只有"其他错误"才标失败
    const authCodes = [401, 1004, 1005]
    if (!authCodes.includes(e?.code)) {
      loadFailed.value = true
    }
  } finally {
    loading.value = false
  }
}

export function useProfile() {
  /**
   * 保存资料（mock：只写当前账号的 localStorage）。
   * 传的是「和默认值不同的字段」，比如 { school: 'xx' }。
   * 注意：nickName 现在不走这里——昵称只认账号（登录/数据库那份），
   * 本地没有真正的"改昵称"入口，等后端资料接口做好再放开。
   */
  function updateProfile(patch) {
    profile.value = { ...profile.value, ...patch }
    const key = storageKey()
    if (key) localStorage.setItem(key, JSON.stringify(profile.value))
  }

  return {
    /** 用户编辑过的资料字段（昵称/头像/性别/生日/学校/简介），和登录信息合并展示 */
    profile,
    updateProfile,

    // —— 四个 tab 列表（都接真实接口） ——
    myVideos,
    likedVideos,
    collectedVideos,
    coinedVideos,
    loading,
    loadFailed,
    loadProfileVideos
  }
}
