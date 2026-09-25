/**
 * 通用格式化工具
 */

/**
 * 格式化播放量（中文化简写）
 * @param {number} count
 * @returns {string} 例：1284000 -> "128.4万"
 */
export function formatCount(count) {
  const num = Number(count)
  if (!Number.isFinite(num) || num < 0) return '0'
  if (num >= 100000000) return trimZero((num / 100000000).toFixed(1)) + '亿'
  if (num >= 10000) return trimZero((num / 10000).toFixed(1)) + '万'
  return String(num)
}

/**
 * 格式化视频时长（秒 -> mm:ss）
 * @param {number} seconds
 * @returns {string} 例：754 -> "12:34"
 */
export function formatDuration(seconds) {
  const total = Math.max(0, Math.floor(Number(seconds) || 0))
  const minutes = Math.floor(total / 60)
  const rest = total % 60
  return `${minutes}:${String(rest).padStart(2, '0')}`
}

/** 去掉小数末尾的 .0，如 "1.0" -> "1" */
function trimZero(str) {
  return str.replace(/\.0$/, '')
}

/**
 * 格式化日期（datetime 字符串 -> YYYY-MM-DD）
 * @param {string} dateTime 例："2026-09-01T10:00:00" -> "2026-09-01"
 */
export function formatDate(dateTime) {
  if (!dateTime) return ''
  const date = new Date(dateTime)
  if (Number.isNaN(date.getTime())) return ''
  const m = String(date.getMonth() + 1).padStart(2, '0')
  const d = String(date.getDate()).padStart(2, '0')
  return `${date.getFullYear()}-${m}-${d}`
}

/**
 * 格式化时间（datetime 字符串 -> HH:MM，聊天消息时间戳用）
 * @param {string|number} dateTime 传空 = 当前时间；非法值返回 ''
 */
export function formatTime(dateTime) {
  const date = dateTime ? new Date(dateTime) : new Date()
  if (Number.isNaN(date.getTime())) return ''
  const h = String(date.getHours()).padStart(2, '0')
  const m = String(date.getMinutes()).padStart(2, '0')
  return `${h}:${m}`
}
