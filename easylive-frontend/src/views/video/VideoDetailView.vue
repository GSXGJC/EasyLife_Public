<script setup>
import { computed, onBeforeUnmount, onMounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import BaseIcon from '@/components/common/BaseIcon.vue'
import { getVideoDetail } from '@/api/video.js'
import {
  likeVideo,
  unlikeVideo,
  favoriteVideo,
  unfavoriteVideo,
  coinVideo,
  getVideoInteract
} from '@/api/interaction.js'
import { useAuth } from '@/composables/useAuth.js'
import { getFollowStatus, followUser, unfollowUser } from '@/api/follow.js'
import { formatCount, formatDate } from '@/utils/format.js'

const route = useRoute()
const router = useRouter()
const { isLoggedIn, myUserId, showLogin } = useAuth()

const videoId = route.params.videoId
const video = ref(null)
const loading = ref(true)
const error = ref('')
const videoRef = ref(null) // 播放器 DOM 引用，自动播放要用

// —— 互动状态：记录"我点过赞没/收藏过没/投过币没 + 硬币余额 + 最新计数" ——
// 后端 GET /video/interact/{id} 返回，操作成功后也用它的返回值刷新这里
const interact = ref(null)
const coinPanelVisible = ref(false)
const notice = ref('')

// —— 关注 UP 主：following=true 显示"已关注"；看自己视频不显示按钮 ——
const following = ref(false)
const isSelf = computed(() =>
  isLoggedIn.value && myUserId.value && myUserId.value === video.value?.userId
)

onMounted(async () => {
  try {
    video.value = await getVideoDetail(videoId)
    loadInteract()
    loadFollowStatus()
  } catch (err) {
    error.value = err?.message || '视频加载失败'
  } finally {
    loading.value = false
  }
})

// 登录状态变化（比如在当前页面点登录弹窗登完）：重新拉一次互动状态和关注状态，
// 否则登录后按钮还是"未激活"的样子
watch(isLoggedIn, (val) => {
  if (val) {
    loadInteract()
    loadFollowStatus()
  } else {
    following.value = false
  }
})

/** 拉当前用户是否已关注这个 UP 主（没登录/看自己时不调） */
async function loadFollowStatus() {
  if (!isLoggedIn.value || !myUserId.value || isSelf.value) return
  try {
    following.value = await getFollowStatus(video.value.userId)
  } catch {
    // 拉关注状态失败不影响看视频，按钮保持"关注"可点
  }
}

/** 关注 / 取消关注 */
async function onToggleFollow() {
  if (!isLoggedIn.value) {
    showLogin()
    return
  }
  if (isSelf.value || !video.value?.userId) return
  try {
    if (following.value) {
      await unfollowUser(video.value.userId)
      following.value = false
      showNotice('已取消关注')
    } else {
      await followUser(video.value.userId)
      following.value = true
      showNotice('关注成功')
    }
  } catch (e) {
    if (!isAuthError(e)) showNotice(e?.message || '操作失败，请重试')
  }
}

onBeforeUnmount(() => clearTimeout(noticeTimer))

let noticeTimer = null
function showNotice(msg) {
  notice.value = msg
  clearTimeout(noticeTimer)
  noticeTimer = setTimeout(() => (notice.value = ''), 3000)
}

/** 拉取当前用户对该视频的互动状态 */
async function loadInteract() {
  try {
    interact.value = await getVideoInteract(videoId)
    syncCounts()
  } catch (e) {
    // 状态接口失败不阻塞页面：按钮保持"未激活"可点，操作接口会兜底报错
  }
}

/** 把接口返回的最新计数同步到 video 上（详情页数字来自 video） */
function syncCounts() {
  if (!video.value || !interact.value) return
  video.value.likeCount = interact.value.likeCount
  video.value.coinCount = interact.value.coinCount
  video.value.collectCount = interact.value.collectCount
}

/**
 * 判断是否"登录失效"错误（code 401/1004/1005）。
 * 这种错误 http.js 已经自动登出并弹出登录框了，这里就不用再弹一条提示，
 * 避免"弹窗 + toast"双重打扰。
 */
function isAuthError(e) {
  return [401, 1004, 1005].includes(e?.code)
}

// —— 点赞 / 取消点赞（再点一次就是取消） ——
async function onLike() {
  if (!isLoggedIn.value) {
    showLogin()
    return
  }
  try {
    interact.value = interact.value?.liked
      ? await unlikeVideo(videoId)
      : await likeVideo(videoId)
    syncCounts()
  } catch (e) {
    if (!isAuthError(e)) showNotice(e?.message || '操作失败，请重试')
  }
}

// —— 收藏 / 取消收藏 ——
async function onFavorite() {
  if (!isLoggedIn.value) {
    showLogin()
    return
  }
  try {
    interact.value = interact.value?.collected
      ? await unfavoriteVideo(videoId)
      : await favoriteVideo(videoId)
    syncCounts()
  } catch (e) {
    if (!isAuthError(e)) showNotice(e?.message || '操作失败，请重试')
  }
}

// —— 投币：先弹面板（选 1 还是 2 个），再调接口 ——
function onCoinClick() {
  if (!isLoggedIn.value) {
    showLogin()
    return
  }
  if (interact.value?.coined) {
    showNotice('你已经给这个视频投过币啦')
    return
  }
  coinPanelVisible.value = !coinPanelVisible.value
}

async function onCoin(coinCount) {
  try {
    interact.value = await coinVideo(videoId, coinCount)
    coinPanelVisible.value = false
    syncCounts()
    showNotice(`投币成功，当前硬币 ${interact.value.currentCoinCount}`)
  } catch (e) {
    if (!isAuthError(e)) showNotice(e?.message || '投币失败，请重试')
  }
}

/**
 * 视频元数据加载好后自动播放。
 * 浏览器自动播放策略：带声音的自动播放会被拦截（play() 的 Promise 会 reject），
 * 此时降级成静音播放兜底，保证"进来就能播"；用户点开音量后声音恢复。
 */
function tryPlay() {
  const el = videoRef.value
  if (!el) return
  el.play().catch(() => {
    el.muted = true
    el.play()
  })
}

function goBack() {
  if (window.history.length > 1) {
    router.back()
  } else {
    router.push('/home')
  }
}

/** 默认头像：取 up 昵称首字符（upAvatar 为空时用） */
function avatarInitial(name) {
  return (name || 'U').charAt(0).toUpperCase()
}
</script>

<template>
  <div class="video-detail">
    <!-- 加载中 -->
    <div v-if="loading" class="video-detail__status">加载中...</div>

    <!-- 加载失败 / 视频不存在 -->
    <div v-else-if="error" class="video-detail__status">
      <p>{{ error }}</p>
    </div>

    <template v-else>
      <button class="video-detail__back" type="button" @click="goBack">
        <BaseIcon name="arrow-left" :size="18" />
        返回
      </button>

      <!-- 播放区：有视频地址就放 <video>，否则用封面占位 -->
      <div class="video-detail__player">
        <video
          ref="videoRef"
          v-if="video.videoUrl"
          class="video-detail__video"
          :src="video.videoUrl"
          controls
          playsinline
          preload="auto"
          @loadedmetadata="tryPlay"
        ></video>
        <img v-else class="video-detail__cover" :src="video.cover" :alt="video.title" />
      </div>

      <!-- 标题 -->
      <h1 class="video-detail__title">{{ video.title }}</h1>

      <!-- 作者 + 播放量/日期 -->
      <div class="video-detail__info">
        <div class="video-detail__up">
          <img
            v-if="video.upAvatar"
            class="video-detail__avatar"
            :src="video.upAvatar"
            :alt="video.upName"
          />
          <div v-else class="video-detail__avatar video-detail__avatar--text">
            {{ avatarInitial(video.upName) }}
          </div>
          <div>
            <p class="video-detail__up-name">{{ video.upName }}</p>
            <p class="video-detail__meta">
              {{ formatCount(video.playCount) }}次播放 ·
              {{ formatCount(video.commentCount) }}评论 ·
              {{ formatDate(video.createTime) }}发布
            </p>
          </div>

          <!-- 关注 / 已关注：看自己的视频时不显示 -->
          <button
            v-if="!isSelf"
            type="button"
            class="video-detail__follow"
            :class="{ 'is-following': following }"
            @click="onToggleFollow"
          >
            {{ following ? '已关注' : '关注' }}
          </button>
        </div>

        <!-- 点赞 / 投币 / 收藏：已接真实接口 -->
        <div class="video-detail__actions">
          <!-- 点赞：再点一次 = 取消，激活时变粉色 -->
          <button
            type="button"
            class="video-detail__action"
            :class="{ 'is-like': interact?.liked }"
            @click="onLike"
          >
            <BaseIcon name="like" :size="16" />
            <span>{{ interact?.liked ? '已点赞' : '点赞' }}</span>
            <span class="video-detail__action-count">{{ formatCount(video.likeCount) }}</span>
          </button>

          <!-- 投币：激活后显示"已投币"，点击弹出面板 -->
          <div class="video-detail__coin">
            <button
              type="button"
              class="video-detail__action"
              :class="{ 'is-coin': interact?.coined }"
              @click="onCoinClick"
            >
              <BaseIcon name="coin" :size="16" />
              <span>{{ interact?.coined ? '已投币' : '投币' }}</span>
              <span class="video-detail__action-count">{{ formatCount(video.coinCount) }}</span>
            </button>

            <div v-if="coinPanelVisible" class="video-detail__coin-panel">
              <p class="video-detail__coin-balance">
                我的硬币 <b>{{ interact?.currentCoinCount ?? 0 }}</b>
              </p>
              <div class="video-detail__coin-options">
                <button type="button" @click="onCoin(1)">投 1 枚</button>
                <button type="button" @click="onCoin(2)">投 2 枚</button>
              </div>
            </div>
          </div>

          <!-- 收藏：再点一次 = 取消，激活时变主色蓝 -->
          <button
            type="button"
            class="video-detail__action"
            :class="{ 'is-collect': interact?.collected }"
            @click="onFavorite"
          >
            <BaseIcon name="collect" :size="16" />
            <span>{{ interact?.collected ? '已收藏' : '收藏' }}</span>
            <span class="video-detail__action-count">{{ formatCount(video.collectCount) }}</span>
          </button>
        </div>
      </div>

      <!-- 操作结果提示（轻量 toast，无全局依赖） -->
      <p v-if="notice" class="video-detail__notice">{{ notice }}</p>

      <!-- 简介 -->
      <div class="video-detail__desc">
        <p v-if="video.description">{{ video.description }}</p>
        <p v-else class="video-detail__desc-empty">这个视频很神秘，up 主什么都没有写。</p>
      </div>
    </template>
  </div>
</template>

<style scoped>
.video-detail {
  max-width: 960px;
  margin: 0 auto;
}

.video-detail__status {
  padding: var(--space-10) 0;
  text-align: center;
  color: var(--color-text-muted);
}

/* 返回按钮 */
.video-detail__back {
  display: inline-flex;
  align-items: center;
  gap: var(--space-1);
  margin-bottom: var(--space-3);
  padding: var(--space-1) var(--space-2);
  border-radius: var(--radius-sm);
  color: var(--color-text-secondary);
  font-size: var(--font-size-sm);
  transition: background-color var(--transition-fast), color var(--transition-fast);
}

.video-detail__back:hover {
  background-color: var(--color-bg-hover);
  color: var(--color-text);
}

/* 播放区 */
.video-detail__player {
  aspect-ratio: 16 / 9;
  border-radius: var(--radius-lg);
  overflow: hidden;
  background-color: var(--color-bg-elevated);
  box-shadow: var(--shadow-card);
}

.video-detail__video,
.video-detail__cover {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

/* 标题 */
.video-detail__title {
  margin-top: var(--space-4);
  font-size: var(--font-size-title);
  font-weight: 600;
  line-height: 1.4;
  color: var(--color-text);
}

/* 作者 + 操作 */
.video-detail__info {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: var(--space-4);
  flex-wrap: wrap;
  margin-top: var(--space-4);
  padding-bottom: var(--space-4);
  border-bottom: 1px solid var(--color-border-strong);
}

.video-detail__up {
  display: flex;
  align-items: center;
  gap: var(--space-3);
}

.video-detail__avatar {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 44px;
  height: 44px;
  border-radius: 50%;
  background-color: var(--color-bg-elevated);
  object-fit: cover;
  flex-shrink: 0;
}

.video-detail__avatar--text {
  background: linear-gradient(135deg, var(--color-primary), var(--color-primary-hover));
  color: #ffffff;
  font-size: 18px;
  font-weight: 600;
}

.video-detail__up-name {
  font-size: var(--font-size-base);
  font-weight: 500;
  color: var(--color-text);
}

.video-detail__meta {
  margin-top: 2px;
  font-size: var(--font-size-sm);
  color: var(--color-text-muted);
}

/* 关注 / 已关注（在 UP 信息块右侧） */
.video-detail__follow {
  flex-shrink: 0;
  padding: var(--space-1) var(--space-4);
  border: 1px solid var(--color-primary);
  border-radius: 999px;
  color: var(--color-primary);
  font-weight: 500;
  transition:
    border-color var(--transition-fast),
    color var(--transition-fast),
    background-color var(--transition-fast);
}

.video-detail__follow:hover {
  background-color: rgba(0, 174, 236, 0.08);
}

/* 已关注：降级成灰色实底，表示"已在关注列表里" */
.video-detail__follow.is-following {
  border-color: var(--color-border-strong);
  color: var(--color-text-secondary);
  background-color: var(--color-bg-hover);
}

/* ---------- 三个互动按钮 ---------- */
.video-detail__actions {
  display: flex;
  align-items: center;
  gap: var(--space-2);
  flex-wrap: wrap;
}

.video-detail__action {
  display: inline-flex;
  align-items: center;
  gap: var(--space-1);
  padding: var(--space-2) var(--space-3);
  border: 1px solid var(--color-border-strong);
  border-radius: 999px;
  color: var(--color-text-secondary);
  transition:
    border-color var(--transition-fast),
    color var(--transition-fast),
    background-color var(--transition-fast);
}

.video-detail__action:hover {
  border-color: var(--color-text-secondary);
  color: var(--color-text);
  background-color: var(--color-bg-hover);
}

.video-detail__action-count {
  font-weight: 600;
}

/* 点赞激活：粉色 */
.video-detail__action.is-like {
  border-color: var(--color-danger);
  color: var(--color-danger);
  background-color: rgba(251, 114, 153, 0.08);
}

/* 投币激活：金色 */
.video-detail__action.is-coin {
  border-color: var(--color-coin);
  color: var(--color-coin);
  background-color: rgba(245, 179, 66, 0.08);
}

/* 收藏激活：主色蓝 */
.video-detail__action.is-collect {
  border-color: var(--color-primary);
  color: var(--color-primary);
  background-color: rgba(0, 174, 236, 0.08);
}

/* 投币面板：相对按钮定位的下拉小卡片 */
.video-detail__coin {
  position: relative;
}

.video-detail__coin-panel {
  position: absolute;
  right: 0;
  top: calc(100% + 6px);
  z-index: 10;
  min-width: 180px;
  padding: var(--space-3);
  border-radius: var(--radius-md);
  background-color: var(--color-bg-elevated);
  border: 1px solid var(--color-border-strong);
  box-shadow: var(--shadow-card);
}

.video-detail__coin-balance {
  margin-bottom: var(--space-2);
  font-size: var(--font-size-sm);
  color: var(--color-text-secondary);
}

.video-detail__coin-balance b {
  color: var(--color-coin);
  font-size: var(--font-size-base);
}

.video-detail__coin-options {
  display: flex;
  gap: var(--space-2);
}

.video-detail__coin-options button {
  flex: 1;
  padding: var(--space-1) var(--space-2);
  border-radius: 999px;
  border: 1px solid var(--color-coin);
  color: var(--color-coin);
  transition: background-color var(--transition-fast), color var(--transition-fast);
}

.video-detail__coin-options button:hover {
  background-color: var(--color-coin);
  color: #ffffff;
}

/* 操作提示 */
.video-detail__notice {
  margin-top: var(--space-2);
  font-size: var(--font-size-sm);
  color: var(--color-danger);
}

/* 简介 */
.video-detail__desc {
  margin-top: var(--space-4);
  padding: var(--space-4);
  border-radius: var(--radius-md);
  background-color: var(--color-bg-elevated);
  color: var(--color-text-secondary);
  line-height: 1.7;
  white-space: pre-wrap;
}

.video-detail__desc-empty {
  color: var(--color-text-muted);
}
</style>
