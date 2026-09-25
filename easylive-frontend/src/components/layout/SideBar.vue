<script setup>
import { useRouter } from 'vue-router'
import BaseIcon from '@/components/common/BaseIcon.vue'
import { useTheme } from '@/composables/useTheme.js'
import { useAuth } from '@/composables/useAuth.js'
import { useChat } from '@/composables/useChat.js'

const router = useRouter()
const { theme, toggleTheme } = useTheme()
const { isLoggedIn, showLogin } = useAuth()
const { openDrawer, closeDrawer, drawerVisible } = useChat()

const NAV_ITEMS = [
  { path: '/home', label: '首页', icon: 'home' },
  { path: '/feed', label: '动态', icon: 'rss' },
  { path: '/profile', label: '我的', icon: 'user', requiresAuth: true }
]

/** 需要登录的导航项：未登录时拦截并弹出登录框 */
function onNavClick(item, event) {
  if (item.requiresAuth && !isLoggedIn.value) {
    event.preventDefault()
    showLogin()
  }
}

function goBack() {
  if (window.history.length > 1) {
    router.back()
  } else {
    router.push('/home')
  }
}

function onUpload() {
  // 投稿需要登录：未登录先弹登录框，登录后再点一次就进上传页
  if (!isLoggedIn.value) {
    showLogin()
    return
  }
  router.push('/upload')
}

function onMessage() {
  // 私信需要登录：未登录先弹登录框
  if (!isLoggedIn.value) {
    showLogin()
    return
  }
  // 已登录：点一下开抽屉，再点一下关
  if (drawerVisible.value) closeDrawer()
  else openDrawer()
}
</script>

<template>
  <aside class="sidebar">
    <!-- 顶部：返回按钮 + 导航 -->
    <div class="sidebar__top">
      <button class="sidebar__icon-btn" type="button" title="返回" @click="goBack">
        <BaseIcon name="arrow-left" :size="22" />
      </button>

      <div class="sidebar__divider"></div>

      <nav class="sidebar__nav">
        <RouterLink
          v-for="item in NAV_ITEMS"
          :key="item.path"
          :to="item.path"
          class="sidebar__item"
          active-class="sidebar__item--active"
          @click="onNavClick(item, $event)"
        >
          <BaseIcon :name="item.icon" :size="20" />
          <span class="sidebar__label">{{ item.label }}</span>
        </RouterLink>
      </nav>
    </div>

    <!-- 底部：从下到上依次为 设置 / 主题 / 私信 / 上传视频 -->
    <div class="sidebar__bottom">
      <button class="sidebar__upload" type="button" @click="onUpload">
        <BaseIcon name="upload" :size="18" />
        <span>投稿</span>
      </button>

      <button class="sidebar__icon-btn" type="button" title="私信" @click="onMessage">
        <BaseIcon name="message" :size="20" />
      </button>
      <button
        class="sidebar__icon-btn"
        type="button"
        :title="theme === 'dark' ? '切换到亮色' : '切换到暗色'"
        @click="toggleTheme"
      >
        <BaseIcon :name="theme === 'dark' ? 'sun' : 'moon'" :size="20" />
      </button>
      <button class="sidebar__icon-btn" type="button" title="设置">
        <BaseIcon name="settings" :size="20" />
      </button>
    </div>
  </aside>
</template>

<style scoped>
.sidebar {
  width: var(--sidebar-width);
  height: 100vh;
  flex-shrink: 0;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  padding: var(--space-4) var(--space-2);
  background-color: var(--color-bg-sidebar);
  border-right: 1px solid var(--color-border);
}

.sidebar__top {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: var(--space-3);
}

.sidebar__divider {
  width: 80%;
  height: 1px;
  background-color: var(--color-border);
}

.sidebar__nav {
  width: 100%;
  display: flex;
  flex-direction: column;
  gap: var(--space-1);
}

.sidebar__item {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: var(--space-2);
  padding: var(--space-2) var(--space-1);
  border-radius: var(--radius-md);
  color: var(--color-text-secondary);
  transition: background-color var(--transition-fast), color var(--transition-fast);
}

.sidebar__item:hover {
  background-color: var(--color-bg-hover);
  color: var(--color-text);
}

.sidebar__item--active {
  color: var(--color-primary);
  font-weight: 500;
}

.sidebar__bottom {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: var(--space-2);
}

/* 上传视频（投稿）按钮 */
.sidebar__upload {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: var(--space-1);
  width: auto;
  padding: var(--space-1) var(--space-3);
  border-radius: 999px;
  background-color: var(--color-danger);
  color: #ffffff;
  font-weight: 500;
  transition: background-color var(--transition-fast), transform var(--transition-fast);
}

.sidebar__upload:hover {
  background-color: var(--color-danger-hover);
  transform: translateY(-1px);
}

/* 通用圆形图标按钮 */
.sidebar__icon-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 40px;
  height: 40px;
  border-radius: 50%;
  color: var(--color-text-secondary);
  transition: background-color var(--transition-fast), color var(--transition-fast);
}

.sidebar__icon-btn:hover {
  background-color: var(--color-bg-hover);
  color: var(--color-text);
}
</style>
