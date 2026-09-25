/**
 * 路由配置
 * 首页 / 动态 / 我的 三个一级路由，后续新增页面在此扩展。
 */
import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  { path: '/', redirect: '/home' },
  {
    path: '/home',
    name: 'home',
    component: () => import('@/views/home/HomeView.vue'),
    meta: { title: '首页' }
  },
  {
    path: '/feed',
    name: 'feed',
    component: () => import('@/views/feed/FeedView.vue'),
    meta: { title: '动态' }
  },
  {
    path: '/profile',
    name: 'profile',
    component: () => import('@/views/profile/ProfileView.vue'),
    meta: { title: '我的' }
  },
  {
    path: '/video/:videoId',
    name: 'video-detail',
    component: () => import('@/views/video/VideoDetailView.vue'),
    meta: { title: '视频详情' }
  },
  {
    path: '/upload',
    name: 'upload',
    component: () => import('@/views/upload/UploadView.vue'),
    meta: { title: '投稿' }
  },
  // 兜底：未匹配路径回到首页
  { path: '/:pathMatch(.*)*', redirect: '/home' }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 根据路由 meta 同步浏览器标题
router.afterEach((to) => {
  const BASE_TITLE = 'easylive'
  document.title = to.meta.title ? `${to.meta.title} · ${BASE_TITLE}` : BASE_TITLE
})

export default router
