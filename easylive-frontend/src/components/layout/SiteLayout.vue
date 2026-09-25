<script setup>
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import SideBar from '@/components/layout/SideBar.vue'
import TopBar from '@/components/layout/TopBar.vue'
import LoginModal from '@/components/common/LoginModal.vue'
import ChatDrawer from '@/components/chat/ChatDrawer.vue'

const route = useRoute()

// 推荐/热门/搜索顶栏只在首页显示；其他页面（动态/我的/视频详情/投稿）不显示
const showTopBar = computed(() => route.name === 'home')
</script>

<template>
  <div class="site-layout">
    <!-- 左侧栏：全高，固定宽度 -->
    <SideBar />

    <!-- 右侧：顶栏 + 页面内容（顶栏从侧栏右侧开始，不覆盖侧栏） -->
    <div class="site-layout__main">
      <TopBar v-if="showTopBar" />
      <main class="site-layout__content">
        <RouterView />
      </main>
    </div>

    <!-- 全局登录弹窗（Teleport 到 body） -->
    <LoginModal />
    <!-- 全局私信抽屉（常驻挂载，开关与连接状态在 useChat 里） -->
    <ChatDrawer />
  </div>
</template>

<style scoped>
.site-layout {
  display: flex;
  height: 100vh;
  overflow: hidden;
}

.site-layout__main {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
}

.site-layout__content {
  flex: 1;
  overflow-y: auto;
  padding: var(--space-4) var(--space-6) var(--space-6);
}
</style>
