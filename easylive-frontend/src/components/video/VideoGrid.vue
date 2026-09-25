<script setup>
import VideoCard from '@/components/video/VideoCard.vue'

defineProps({
  videos: { type: Array, required: true },
  loading: { type: Boolean, default: false }
})
</script>

<template>
  <!-- 加载中 -->
  <div v-if="loading" class="video-grid video-grid--status">
    <p>加载中...</p>
  </div>

  <!-- 空列表 -->
  <div v-else-if="videos.length === 0" class="video-grid video-grid--status">
    <p>暂无视频</p>
  </div>

  <!-- 视频网格：5 列 × N 行 -->
  <div v-else class="video-grid">
    <VideoCard v-for="video in videos" :key="video.videoId" :video="video" />
  </div>
</template>

<style scoped>
.video-grid {
  display: grid;
  grid-template-columns: repeat(5, minmax(0, 1fr));
  gap: var(--space-5) var(--space-4);
}

.video-grid--status {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 300px;
  color: var(--color-text-muted);
}
</style>
