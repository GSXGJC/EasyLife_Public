/**
 * 聊天记录相关接口。
 * 后端返回 Result，data 为消息记录列表；
 * 这些接口在 /chat 下，http.js 会自动带 token，未登录会被拦成 401 自动登出弹框。
 */
import { request } from '@/api/http.js'

/**
 * 我和某个 userId 之间的聊天历史记录。
 * 后端 SQL 按 time 倒序 LIMIT 100（最新的在前），每项是原始字段：
 *   { messageFrom, messageTo, messageContent, time }  // time 为 "2026-09-05T10:11:12" 格式
 * @param {string} peerUserId 对方 userId
 * @returns {Promise<Array>} 倒序（新在前）的原始消息记录数组
 */
export function getChatRecords(peerUserId) {
  return request(`/chat/chatRecords/${peerUserId}`).then((r) => r.data)
}
