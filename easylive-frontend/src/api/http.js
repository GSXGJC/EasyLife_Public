/**
 * 轻量 HTTP 请求封装（基于 fetch，无额外依赖）
 *
 * 后端 easylive-web 使用「统一响应封装」：所有接口都返回 Result 结构
 *   { code, message, data }
 * - code === SUCCESS_CODE(200)   表示成功，data 为业务数据（VO）
 * - code === 1002 等非 200       表示业务失败，message 为失败原因
 * 这里统一解析 Result，非 200 直接抛错，调用方只需 try/catch 展示 message。
 */

const API_BASE = '/api'

/** 与 useAuth.js 里的 TOKEN_KEY 保持一致 */
const TOKEN_KEY = 'easylive-token'

/** 后端 Result 中表示成功的状态码（与后端 Result.success 对应） */
export const SUCCESS_CODE = 200

/** 业务失败抛出的错误，message 直接来自后端，可展示给用户 */
export class BusinessError extends Error {}

/**
 * 需要"重新登录"的后端状态码：
 * 401 未登录 / 1004 Token已过期 / 1005 Token无效。
 * 请求命中这些码 = 本地登录态已失效，统一交给 unauthorizedHandler 处理（登出+弹登录框）。
 */
const AUTH_ERROR_CODES = new Set([401, 1004, 1005])

/**
 * 登录失效时的提示文案。
 * 注意：面向用户，绝口不提"token"这种技术词，用户看不懂。
 */
const UNAUTHORIZED_MESSAGE = '登录已失效，请重新登录'

/** 由 useAuth 注入的回调：收到登录失效响应时，登出并弹出登录框 */
let unauthorizedHandler = null

/** useAuth 调用：注册"登录失效"处理函数 */
export function setUnauthorizedHandler(handler) {
  unauthorizedHandler = handler
}

/**
 * 发请求并解析统一 Result
 * @param {string} path  接口路径（自动带 /api 前缀，如 '/user/login'）
 * @param {{ method?: string, body?: object }} [options]
 * @returns {Promise<{ code: number, message: string, data: any }>} 解析后的 Result
 */
export async function request(path, { method = 'GET', body } = {}) {
  const isFormData = body instanceof FormData
  const headers = {}
  // multipart/form-data 的 Content-Type 和 boundary 必须由浏览器自动生成，
  // 手动指定会导致后端解析不到文件。所以只有 JSON body 才手动设 Content-Type
  if (!isFormData) headers['Content-Type'] = 'application/json'
  // 已登录则带上 token，供后端 MyInterceptor 校验
  // （token 由 useAuth.login 登录成功后写入 localStorage）
  const token = localStorage.getItem(TOKEN_KEY)
  if (token) headers['token'] = `Bearer ${token}`

  let res
  try {
    res = await fetch(`${API_BASE}${path}`, {
      method,
      headers,
      body: body ? (isFormData ? body : JSON.stringify(body)) : undefined
    })
  } catch {
    // 后端未启动 / 网络不通
    throw new BusinessError('网络异常，请确认后端服务已启动')
  }

  if (!res.ok) {
    // HTTP 状态不是 2xx：body 可能是 Result JSON（如 413/500 带 {code,message}），
    // 也可能是纯文本/HTML（如拦截器返回"请先登录"）。
    // 优先尝试从 JSON 里取 code/message，取不到再退回原始文本。
    let message = `请求失败（HTTP ${res.status}）`
    let code = res.status
    try {
      const text = await res.text()
      if (text) {
        try {
          const parsed = JSON.parse(text)
          message = parsed?.message || text
          code = parsed?.code ?? res.status
        } catch {
          message = text
        }
      }
    } catch {
      // 读不到正文就用默认提示
    }
    if (AUTH_ERROR_CODES.has(code)) {
      unauthorizedHandler?.()
      // 面向用户的提示：不说"token"，只说登录状态没了
      const authErr = new BusinessError(UNAUTHORIZED_MESSAGE)
      authErr.code = code
      throw authErr
    }
    throw new BusinessError(message)
  }

  const result = await res.json()

  // 统一判定业务成功 / 失败
  if (result.code !== SUCCESS_CODE) {
    // 登录失效：通知 useAuth 登出并弹登录框，抛出的文案用用户能懂的话
    if (AUTH_ERROR_CODES.has(result.code)) {
      unauthorizedHandler?.()
      const authErr = new BusinessError(UNAUTHORIZED_MESSAGE)
      authErr.code = result.code
      throw authErr
    }
    throw new BusinessError(result.message || '请求失败，请重试')
  }

  return result
}
