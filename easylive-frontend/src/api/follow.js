/**
 * 关注相关接口。
 * 后端返回 Result，data 为 Boolean 或用户列表；
 * 这些接口都在 /user 下，http.js 会自动带 token，未登录会被拦成 401 自动登出弹框。
 */
import { request } from '@/api/http.js'

/**
 * 关注一个用户（幂等：重复关注返回 data=true 不报错）
 * @param {string} userId 目标用户 id
 * @returns {Promise<boolean>} 关注后是否为"已关注"
 */
export function followUser(userId) {
  return request(`/user/follow/${userId}`, { method: 'POST' }).then((r) => r.data)
}

/**
 * 取消关注
 * @returns {Promise<boolean>} 取关后是否为"已关注"（恒 false）
 */
export function unfollowUser(userId) {
  return request(`/user/follow/${userId}`, { method: 'DELETE' }).then((r) => r.data)
}

/**
 * 我关注的用户名单（私信面板联系人）
 * @returns {Promise<Array<{userId:string, nickName:string, avatar:string|null}>>}
 */
export function getFollowList() {
  return request('/user/follow/list').then((r) => r.data)
}

/**
 * 当前用户是否已关注某人（视频详情页按钮状态）
 * @returns {Promise<boolean>}
 */
export function getFollowStatus(userId) {
  return request(`/user/follow/status/${userId}`).then((r) => r.data)
}
