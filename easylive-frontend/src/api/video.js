/**
 * 视频接口层（对接后端 easylive-web）
 *
 * 后端接口约定（后端 Video 模块未写完时请求会失败）：
 *   GET /api/video/list?page=1&pageSize=15
 *       → Result<{ list: VideoVO[], total: number, hasMore: boolean }>
 *   GET /api/video/{videoId}
 *       → Result<VideoVO>
 *
 * VideoVO 字段（与后端约定一致）：
 *   videoId / title / cover / videoUrl / duration / playCount /
 *   likeCount / commentCount / coinCount / collectCount /
 *   category / createTime / userId / upName / upAvatar
 */
import { request } from '@/api/http.js'

/**
 * 分页获取视频列表
 * @param {{ tab?: 'recommend' | 'hot', page?: number, pageSize?: number }} params
 * @returns {Promise<{ list: Array, total: number, hasMore: boolean }>}
 */
export function getVideoList({ tab = 'recommend', page = 1, pageSize = 15 } = {}) {
  // tab（推荐/热门）后端暂未区分（表里没有 hot 字段），先不传给后端；后续加了再传
  return request(`/video/list?page=${page}&pageSize=${pageSize}`).then((result) => result.data)
}

/**
 * 获取视频详情
 * @param {number|string} videoId
 * @returns {Promise<object>} VideoVO
 */
export function getVideoDetail(videoId) {
  return request(`/video/${videoId}`).then((result) => result.data)
}

/**
 * 我发布的视频列表（「我的投稿」，需登录）。
 * 路径和 /video/liked/list 同一族（.../list），登录校验也一致。
 * @returns {Promise<Array>} VideoVO[]
 */
export function getMyVideos() {
  return request('/video/my/list').then((result) => result.data)
}

/**
 * 发布视频（multipart/form-data，需登录）
 * @param {FormData} formData 至少含 file（视频文件）和 title，可选 cover/description/category/duration
 * 成功时后端返回 Result.success()（data 为空）
 */
export function publishVideo(formData) {
  return request('/video/publish', { method: 'POST', body: formData })
}
