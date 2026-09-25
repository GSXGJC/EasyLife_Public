/**
 * 互动接口层（点赞/收藏/投币 + 我的列表）
 *
 * 后端约定（easylive-web UserInteractionController）：
 *   POST /api/video/like/{videoId}        点赞
 *   POST /api/video/unlike/{videoId}      取消点赞
 *   POST /api/video/favorite/{videoId}    收藏
 *   POST /api/video/unfavorite/{videoId}  取消收藏
 *   POST /api/video/coin/{videoId}?coinCount=1  投币
 *   GET  /api/video/interact/{videoId}    互动状态（公开，可选登录）
 *   GET  /api/video/liked/list            我点赞过的视频
 *   GET  /api/video/favorite/list         我收藏过的视频
 *   GET  /api/video/coined/list           我投过币的视频
 *
 * 点赞/收藏/投币成功后返回 VideoInteractVO（最新按钮状态 + 计数 + 硬币余额），
 * 调用方直接用它刷新页面，不用再请求一次详情。
 */
import { request } from '@/api/http.js'

/** 点赞 */
export function likeVideo(videoId) {
  return request(`/video/like/${videoId}`, { method: 'POST' }).then((r) => r.data)
}

/** 取消点赞 */
export function unlikeVideo(videoId) {
  return request(`/video/unlike/${videoId}`, { method: 'POST' }).then((r) => r.data)
}

/** 收藏 */
export function favoriteVideo(videoId) {
  return request(`/video/favorite/${videoId}`, { method: 'POST' }).then((r) => r.data)
}

/** 取消收藏 */
export function unfavoriteVideo(videoId) {
  return request(`/video/unfavorite/${videoId}`, { method: 'POST' }).then((r) => r.data)
}

/**
 * 投币
 * @param {number|string} videoId
 * @param {number} coinCount 1 或 2，默认 1
 */
export function coinVideo(videoId, coinCount = 1) {
  return request(`/video/coin/${videoId}?coinCount=${coinCount}`, { method: 'POST' }).then(
    (r) => r.data
  )
}

/** 查询当前用户对该视频的互动状态（未登录返回全 false） */
export function getVideoInteract(videoId) {
  return request(`/video/interact/${videoId}`).then((r) => r.data)
}

/** 我点赞过的视频列表 */
export function getLikedVideos() {
  return request('/video/liked/list').then((r) => r.data)
}

/** 我收藏过的视频列表 */
export function getCollectedVideos() {
  return request('/video/favorite/list').then((r) => r.data)
}

/** 我投过币的视频列表 */
export function getCoinedVideos() {
  return request('/video/coined/list').then((r) => r.data)
}
