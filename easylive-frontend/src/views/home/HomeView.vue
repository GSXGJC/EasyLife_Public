<script setup>
import { onBeforeUnmount, onMounted, ref, watch } from 'vue'
import VideoGrid from '@/components/video/VideoGrid.vue'
import { getVideoList } from '@/api/video.js'
import { useHomeTabs } from '@/composables/useHomeTabs.js'

const PAGE_SIZE = 15

const { activeTab } = useHomeTabs()

const videos = ref([])
const page = ref(1)
const hasMore = ref(true)
const loading = ref(false) // 首屏加载
const loadingMore = ref(false) // 下拉加载更多
const sentinel = ref(null) // 底部哨兵元素，用于触发加载更多

let observer = null
let scrollContainer = null

/** 加载第一页（首屏 / 切换页签时） */
async function loadFirstPage() {
  loading.value = true
  try {
    const result = await getVideoList({ tab: activeTab.value, page: 1, pageSize: PAGE_SIZE })
    videos.value = result.list
    page.value = 1
    hasMore.value = result.hasMore
  } catch (error) {
    console.error('加载视频列表失败：', error)
    videos.value = []
    hasMore.value = false
  } finally {
    loading.value = false
  }
}

/** 滚动到底部附近时加载下一页并追加 */
async function loadMore() {
  if (loading.value || loadingMore.value || !hasMore.value) return
  loadingMore.value = true
  try {
    const result = await getVideoList({
      tab: activeTab.value,
      page: page.value + 1,
      pageSize: PAGE_SIZE
    })
    videos.value.push(...result.list)
    page.value += 1
    hasMore.value = result.hasMore
  } catch (error) {
    console.error('加载更多视频失败：', error)
  } finally {
    loadingMore.value = false
  }
}

/** 哨兵元素进入可视范围（距底部 120px）时触发加载 */
function setupObserver() {
  observer = new IntersectionObserver(
    (entries) => {
      if (entries[0]?.isIntersecting) loadMore()
    },
    { rootMargin: '120px 0px' }
  )
  if (sentinel.value) observer.observe(sentinel.value)
}

onMounted(() => {
  loadFirstPage()
  setupObserver()
  scrollContainer = document.querySelector('.site-layout__content')
})

onBeforeUnmount(() => {
  observer?.disconnect()
})

// 切换「推荐 / 热门」页签：重置列表并回到顶部
watch(activeTab, () => {
  loadFirstPage()
  scrollContainer?.scrollTo({ top: 0 })
})
</script>

<template>
  <section class="home-view">
    <VideoGrid :videos="videos" :loading="loading" />

    <!-- 底部状态区：加载更多 / 已到底（同时作为滚动触发的哨兵） -->
    <div ref="sentinel" class="home-view__more">
      <template v-if="loadingMore">
        <span class="home-view__spinner"></span>
        <span>加载中...</span>
      </template>
      <template v-else-if="!hasMore && videos.length > 0">
        <span>— 已经到底啦 —</span>
      </template>
    </div>
  </section>
</template>

<style scoped>
.home-view {
  max-width: 1600px;
  margin: 0 auto;
}

.home-view__more {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: var(--space-2);
  min-height: 56px;
  color: var(--color-text-muted);
  font-size: var(--font-size-sm);
}

.home-view__spinner {
  width: 16px;
  height: 16px;
  border: 2px solid var(--color-border-strong);
  border-top-color: var(--color-primary);
  border-radius: 50%;
  animation: home-view-spin 0.8s linear infinite;
}

@keyframes home-view-spin {
  to {
    transform: rotate(360deg);
  }
}
</style>
