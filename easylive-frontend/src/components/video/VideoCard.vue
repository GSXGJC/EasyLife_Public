<script setup>
import { useRouter } from 'vue-router'
import BaseIcon from '@/components/common/BaseIcon.vue'
import { formatCount, formatDuration, formatDate } from '@/utils/format.js'

const props = defineProps({
  video: { type: Object, required: true }
})

const router = useRouter()

function onCardClick() {
  router.push(`/video/${props.video.videoId}`)
}
</script>

<template>
  <article class="video-card" @click="onCardClick">
    <!-- 封面：有图显示图；没图（cover 为空，比如上传没选封面）显示占位块 -->
    <div class="video-card__cover">
      <img
        v-if="video.cover"
        class="video-card__cover-img"
        :src="video.cover"
        :alt="video.title"
        loading="lazy"
      />
      <div v-else class="video-card__cover-img video-card__placeholder">
        <BaseIcon name="play" :size="32" />
        <span class="video-card__placeholder-title">{{ video.title }}</span>
      </div>
      <div class="video-card__cover-mask"></div>
      <div class="video-card__cover-info">
        <span class="video-card__views">
          <BaseIcon name="play" :size="12" />
          {{ formatCount(video.playCount) }}
        </span>
        <span class="video-card__duration">{{ formatDuration(video.duration) }}</span>
      </div>
    </div>

    <!-- 封面下方：标题 + up名 · 发布日期 -->
    <div class="video-card__body">
      <h3 class="video-card__title" :title="video.title">{{ video.title }}</h3>
      <p class="video-card__meta">
        <span class="video-card__up">{{ video.upName }}</span>
        <span class="video-card__sep">·</span>
        <span class="video-card__date">{{ formatDate(video.createTime) }}</span>
      </p>
    </div>
  </article>
</template>

<style scoped>
.video-card {
  display: flex;
  flex-direction: column;
  cursor: pointer;
}

/* ---------- 封面 ---------- */
.video-card__cover {
  position: relative;
  aspect-ratio: 16 / 9;
  border-radius: var(--radius-md);
  overflow: hidden;
  background-color: var(--color-bg-elevated);
  box-shadow: var(--shadow-card);
}

.video-card__cover-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform var(--transition-base);
}

/* 无封面占位：渐变底 + 居中播放图标 + 标题，替代空图/裂图 */
.video-card__placeholder {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: var(--space-2);
  padding: var(--space-3);
  background: linear-gradient(135deg, var(--color-primary), var(--color-primary-hover));
  color: rgba(255, 255, 255, 0.92);
  text-align: center;
}

.video-card__placeholder-title {
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  font-size: var(--font-size-base);
  font-weight: 500;
  line-height: 1.4;
  max-width: 90%;
}

.video-card:hover .video-card__cover-img {
  transform: scale(1.04);
}

.video-card__cover-mask {
  position: absolute;
  inset: 0;
  background: var(--color-cover-mask);
}

.video-card__cover-info {
  position: absolute;
  left: 0;
  right: 0;
  bottom: 0;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: var(--space-2);
  color: #ffffff;
  font-size: var(--font-size-sm);
  text-shadow: 0 1px 2px rgba(0, 0, 0, 0.5);
}

.video-card__views {
  display: flex;
  align-items: center;
  gap: var(--space-1);
}

.video-card__duration {
  padding: 1px var(--space-1);
  border-radius: var(--radius-sm);
  background-color: rgba(0, 0, 0, 0.6);
  text-shadow: none;
}

/* ---------- 封面下方 ---------- */
.video-card__body {
  padding-top: var(--space-2);
}

.video-card__title {
  font-size: var(--font-size-base);
  font-weight: 500;
  line-height: 1.4;
  color: var(--color-text);
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.video-card:hover .video-card__title {
  color: var(--color-primary);
}

.video-card__meta {
  display: flex;
  align-items: center;
  gap: var(--space-1);
  margin-top: var(--space-1);
  font-size: var(--font-size-sm);
  color: var(--color-text-secondary);
}

.video-card__up {
  max-width: 60%;
  overflow: hidden;
  white-space: nowrap;
  text-overflow: ellipsis;
}

.video-card__up:hover {
  color: var(--color-primary);
}

.video-card__sep {
  color: var(--color-text-muted);
}
</style>
