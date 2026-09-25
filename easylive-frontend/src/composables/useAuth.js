/**
 * 登录状态管理
 * - isLoggedIn   是否已登录（本地是否保存了用户信息）
 * - loginVisible 登录弹窗是否可见
 * - user         当前用户信息（登录成功后后端返回的昵称等写入本地）
 *
 * 登录已对接后端 easylive-web：POST /login，
 * 前端传 DTO（email + password），后端返回 Result.success(vo)，vo 含昵称；
 * 失败返回 code=1002 的 Result，由表单展示后端 message。
 * 注册已对接后端：先调 /register（email + nickName + password），
 * 成功后自动登录拿 token。
 */
import { computed, ref } from 'vue'
import { login as loginRequest, logout as logoutRequest, register as registerRequest } from '@/api/auth.js'
import { setUnauthorizedHandler } from '@/api/http.js'

const TOKEN_KEY = 'easylive-token'
const USER_KEY = 'easylive-user'

/** 读取本地保存的用户信息 */
function getSavedUser() {
  try {
    const raw = localStorage.getItem(USER_KEY)
    return raw ? JSON.parse(raw) : null
  } catch {
    return null
  }
}

// 兼容旧会话：早前登录没存 userId，而关注按钮的"是否自己"、消息气泡的"谁发的"
// 都依赖 userId，缺 userId 的旧本地会话一律清掉，重新登录一次即有。
const savedUser = getSavedUser()
const user = ref(savedUser && savedUser.userId ? savedUser : null)
if (savedUser && !savedUser.userId) {
  localStorage.removeItem(TOKEN_KEY)
  localStorage.removeItem(USER_KEY)
}
const isLoggedIn = ref(Boolean(user.value))
const loginVisible = ref(false)

/** 展示用的用户名（未设置时给默认值） */
const userName = computed(() => user.value?.name || 'easylive 用户')

/** 我自己的 userId（后端 LoginVo 返回；聊天气泡分左右、详情页"不关注自己"都靠它） */
const myUserId = computed(() => user.value?.userId || '')

/** 邮箱"@ 前那段"：abc@xx.com → abc，后端昵称为空时当兜底显示名 */
function emailPrefix(email) {
  const s = String(email || '')
  return s.split('@')[0] || s
}

function saveUser(nextUser) {
  user.value = nextUser
  localStorage.setItem(USER_KEY, JSON.stringify(nextUser))
}

/**
 * 清掉本地登录态（token + 用户信息）。
 * logout() 和 http 层"登录失效自动登出"都走这里，避免两处逻辑写重、以后改漏。
 */
function forceLogout() {
  localStorage.removeItem(TOKEN_KEY)
  localStorage.removeItem(USER_KEY)
  user.value = null
  isLoggedIn.value = false
}

/**
 * 由 http.js 收到"未登录/登录已失效"的响应码时回调：
 * 直接把用户登出并弹出登录框，引导重新登录。
 */
function forceLogin() {
  forceLogout()
  loginVisible.value = true
}
setUnauthorizedHandler(forceLogin)

export function useAuth() {
  function showLogin() {
    loginVisible.value = true
  }

  function hideLogin() {
    loginVisible.value = false
  }

  /**
   * 登录：调用后端接口，成功后保存用户信息
   * @param {{ email: string, password: string }} payload 登录 DTO
   * @returns {Promise<object>} 后端返回的 VO
   * 失败时抛错（code=1002 等），由表单 catch 后展示错误信息
   */
  async function login(payload) {
    const vo = await loginRequest(payload)
    const token = vo.token || `token-${Date.now()}`
    localStorage.setItem(TOKEN_KEY, token)
    saveUser({
      userId: vo.userId, // 后端 LoginVo 返回；聊天气泡分左右、关注按钮判"是否自己"都依赖它
      name: vo.nickName || emailPrefix(payload.email),
      email: payload.email, // LoginVo 不回 email，这里用登录时填的那个
      token
    })
    isLoggedIn.value = true
    hideLogin()
    return vo
  }

  /**
   * 邮箱注册：先调后端注册接口，成功后自动登录（复用 login 流程拿 token 和用户信息）
   * @param {{ email: string, nickName: string, password: string }} payload 注册 DTO
   * 失败时抛错（如邮箱已注册），由表单 catch 后展示错误信息
   */
  async function register(payload) {
    // 1. 调注册接口（后端注册成功不返回数据）
    await registerRequest(payload)
    // 2. 注册成功即自动登录，复用 login 的保存逻辑（token + 用户信息 + 关弹窗）
    return login({ email: payload.email, password: payload.password })
  }

  /**
   * 退出登录：先通知后端删掉这个会话（redis 里的记录），让 token 立即失效，
   * 再清空本地登录态。即使网络失败 / 会话已失效，也照常清空本地，
   * 保证用户点了退出就一定能退出，不把后端异常抛给界面。
   */
  async function logout() {
    try {
      await logoutRequest()
    } catch {
      // 后端没删成也没关系，本地照常退出
    } finally {
      forceLogout()
    }
  }

  return {
    isLoggedIn,
    loginVisible,
    user,
    userName,
    myUserId,
    showLogin,
    hideLogin,
    login,
    register,
    logout
  }
}
