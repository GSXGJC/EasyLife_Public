/**
 * 主题（白 / 暗）管理
 * - 通过根元素 <html data-theme="light|dark"> 切换
 * - 偏好持久化到 localStorage，刷新后保持
 * - 首次访问跟随系统偏好
 */
import { ref, watchEffect } from 'vue'

const THEME_KEY = 'easylive-theme'

function getInitialTheme() {
  const saved = localStorage.getItem(THEME_KEY)
  if (saved === 'dark' || saved === 'light') return saved
  const prefersDark = window.matchMedia?.('(prefers-color-scheme: dark)').matches
  return prefersDark ? 'dark' : 'light'
}

const theme = ref(getInitialTheme())

// 同步到 DOM 并持久化
watchEffect(() => {
  document.documentElement.setAttribute('data-theme', theme.value)
  localStorage.setItem(THEME_KEY, theme.value)
})

export function useTheme() {
  function toggleTheme() {
    theme.value = theme.value === 'dark' ? 'light' : 'dark'
  }

  return { theme, toggleTheme }
}
