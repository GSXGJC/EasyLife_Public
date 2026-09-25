/**
 * 认证相关接口
 *
 * 登录：前端传 DTO（email + password），后端核对后返回 Result.success(vo)，
 * vo 中包含昵称（nickName）等用户信息；失败时返回 code=1002 的 Result。
 */
import { request } from '@/api/http.js'

/**
 * 发送邮箱验证码（注册前校验邮箱归属）
 * 后端把 6 位验证码写进 redis（key=邮箱，约 3 分钟有效）并异步发信到该邮箱
 * @param {string} email 目标邮箱
 */
export function sendEmailCode(email) {
  return request(`/email?email=${encodeURIComponent(email)}`, { method: 'POST' })
}

/**
 * 登录
 * @param {{ email: string, password: string }} payload 登录 DTO
 * @returns {Promise<object>} 登录成功返回的 VO（含 nickName、userId、token）
 * 失败时抛 BusinessError，message 为后端返回的失败原因
 */
export function login(payload) {
  return request('/login', { method: 'POST', body: payload }).then((result) => result.data)
}

/**
 * 注册
 * @param {{ email: string, vertifiCode: string, nickName: string, password: string }} payload 注册 DTO
 * vertifiCode 是发到邮箱的 6 位验证码（字段名按后端 RegDto，注意少个 c 的拼写）
 * 成功时后端返回 Result.success()（data 为空）；失败（验证码错/邮箱已注册）抛 BusinessError，
 * message 为后端返回的原因
 */
export function register(payload) {
  return request('/register', { method: 'POST', body: payload })
}

/**
 * 退出登录
 * 后端会删掉当前用户的会话记录（redis），让这个 token 立即失效。
 * 无需请求体；token 由 http 层自动带上（localStorage 里那个）。
 */
export function logout() {
  return request('/logout', { method: 'POST' })
}
