<script setup>
import { computed, nextTick, ref, watch } from 'vue'
import BaseIcon from '@/components/common/BaseIcon.vue'
import { useChat } from '@/composables/useChat.js'
import { formatTime } from '@/utils/format.js'

const chat = useChat()

const text = ref('')
const msgListEl = ref(null)
const inputEl = ref(null)

/** 当前会话消息（从 useChat 单例按对端取） */
const activeMessages = computed(() => {
  const peer = chat.activePeer.value
  return peer ? [...chat.messagesOf(peer.userId)] : []
})

/** 头像缺省用昵称首字符 */
function avatarInitial(name) {
  return (name || 'U').charAt(0).toUpperCase()
}

function onSend() {
  if (chat.send(text.value)) text.value = ''
}

function onKeydown(e) {
  if (e.key === 'Enter' && !e.isComposing) {
    e.preventDefault()
    onSend()
  }
}

// 进入新会话 / 消息变化时自动滚到底
watch(
  () => [chat.activePeer.value, activeMessages.value.length],
  async () => {
    await nextTick()
    if (msgListEl.value) msgListEl.value.scrollTop = msgListEl.value.scrollHeight
    inputEl.value?.focus()
  }
)
</script>

<template>
  <Teleport to="body">
    <Transition name="chat-layer">
      <div v-if="chat.drawerVisible.value" class="chat-layer">
        <!-- 半透明遮罩：点空白处关抽屉 -->
        <div class="chat-layer__scrim" @mousedown.self="chat.closeDrawer()"></div>

        <aside class="chat-drawer">
          <!-- ============ 视图一：我关注的人 ============ -->
          <template v-if="!chat.activePeer.value">
            <header class="chat-drawer__header">
              <h2 class="chat-drawer__title">私信</h2>
              <button class="chat-drawer__icon-btn" type="button" title="关闭" @click="chat.closeDrawer()">
                <BaseIcon name="x" :size="18" />
              </button>
            </header>

            <div v-if="chat.follows.value.length" class="chat-drawer__body">
              <button
                v-for="peer in chat.follows.value"
                :key="peer.userId"
                type="button"
                class="chat-contact"
                @click="chat.openPeer(peer)"
              >
                <img v-if="peer.avatar" class="chat-contact__avatar" :src="peer.avatar" :alt="peer.nickName" />
                <div v-else class="chat-contact__avatar chat-contact__avatar--text">
                  {{ avatarInitial(peer.nickName) }}
                </div>
                <span class="chat-contact__name">{{ peer.nickName }}</span>
                <span v-if="chat.unreadByPeer[peer.userId]" class="chat-contact__badge">
                  {{ chat.unreadByPeer[peer.userId] }}
                </span>
              </button>
            </div>
            <div v-else class="chat-drawer__empty">
              <p class="chat-drawer__empty-title">你还没关注任何人</p>
              <p class="chat-drawer__empty-sub">去视频页关注喜欢的 UP 主，就能在这里和 TA 私信</p>
            </div>
          </template>

          <!-- ============ 视图二：和某人的单聊 ============ -->
          <template v-else>
            <header class="chat-drawer__header">
              <button class="chat-drawer__icon-btn" type="button" title="返回名单" @click="chat.backToList()">
                <BaseIcon name="arrow-left" :size="18" />
              </button>
              <img
                v-if="chat.activePeer.value.avatar"
                class="chat-drawer__peer-avatar"
                :src="chat.activePeer.value.avatar"
                :alt="chat.activePeer.value.nickName"
              />
              <div v-else class="chat-drawer__peer-avatar chat-drawer__peer-avatar--text">
                {{ avatarInitial(chat.activePeer.value.nickName) }}
              </div>
              <span class="chat-drawer__peer-name">{{ chat.activePeer.value.nickName }}</span>
              <button class="chat-drawer__icon-btn" type="button" title="关闭" @click="chat.closeDrawer()">
                <BaseIcon name="x" :size="18" />
              </button>
            </header>

            <!-- 消息流 -->
            <div ref="msgListEl" class="chat-drawer__msgs">
              <p v-if="chat.loadingOf(chat.activePeer.value.userId)" class="chat-drawer__hint">正在加载聊天记录…</p>
              <p v-else-if="!activeMessages.length" class="chat-drawer__hint">和 TA 打个招呼吧</p>
              <div
                v-for="(m, i) in activeMessages"
                :key="i"
                class="chat-msg"
                :class="m.mine ? 'chat-msg--mine' : 'chat-msg--theirs'"
              >
                <div class="chat-msg__bubble">{{ m.content }}</div>
                <span class="chat-msg__time">{{ formatTime(m.time) }}</span>
              </div>
            </div>

            <!-- 输入区 -->
            <div class="chat-drawer__inputbar">
              <p v-if="!chat.connected.value" class="chat-drawer__status">正在连接，稍候…</p>
              <div class="chat-drawer__inputrow">
                <input
                  ref="inputEl"
                  v-model="text"
                  class="chat-drawer__input"
                  type="text"
                  maxlength="500"
                  placeholder="说点什么…"
                  @keydown="onKeydown"
                />
                <button class="chat-drawer__send" type="button" title="发送" @click="onSend">
                  <BaseIcon name="send" :size="18" />
                </button>
              </div>
            </div>
          </template>
        </aside>
      </div>
    </Transition>
  </Teleport>
</template>

<style scoped>
.chat-layer {
  position: fixed;
  inset: 0;
  z-index: 940;
}

.chat-layer__scrim {
  position: absolute;
  inset: 0;
  background-color: rgba(0, 0, 0, 0.16);
}

/* 抽屉本体：从左侧窄栏右缘起，占屏宽约 1/4 */
.chat-drawer {
  position: absolute;
  top: 0;
  left: var(--sidebar-width);
  width: 25vw;
  min-width: 300px;
  height: 100vh;
  display: flex;
  flex-direction: column;
  background-color: var(--color-bg);
  border-right: 1px solid var(--color-border);
}

.chat-drawer__header {
  display: flex;
  align-items: center;
  gap: var(--space-2);
  height: 56px;
  padding: 0 var(--space-2);
  border-bottom: 1px solid var(--color-border);
  flex-shrink: 0;
}

.chat-drawer__title {
  flex: 1;
  margin-left: var(--space-2);
  font-size: var(--font-size-lg);
  font-weight: 600;
}

.chat-drawer__icon-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 34px;
  height: 34px;
  border-radius: 50%;
  color: var(--color-text-secondary);
  transition: background-color var(--transition-fast), color var(--transition-fast);
}

.chat-drawer__icon-btn:hover {
  background-color: var(--color-bg-hover);
  color: var(--color-text);
}

.chat-drawer__body {
  flex: 1;
  overflow-y: auto;
  padding: var(--space-2);
}

/* ---- 联系人行 ---- */
.chat-contact {
  display: flex;
  align-items: center;
  gap: var(--space-3);
  width: 100%;
  padding: var(--space-2) var(--space-2);
  border-radius: var(--radius-md);
  color: var(--color-text);
  text-align: left;
  transition: background-color var(--transition-fast);
}

.chat-contact:hover {
  background-color: var(--color-bg-hover);
}

.chat-contact__avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  flex-shrink: 0;
  object-fit: cover;
}

.chat-contact__avatar--text,
.chat-drawer__peer-avatar--text {
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: var(--color-bg-hover);
  color: var(--color-primary);
  font-weight: 600;
}

.chat-contact__name {
  flex: 1;
  min-width: 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  font-size: var(--font-size-base);
}

.chat-contact__badge {
  min-width: 18px;
  height: 18px;
  padding: 0 5px;
  border-radius: 999px;
  background-color: var(--color-danger);
  color: #ffffff;
  font-size: var(--font-size-sm);
  line-height: 18px;
  text-align: center;
}

/* ---- 空名单 ---- */
.chat-drawer__empty {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: var(--space-2);
  padding: var(--space-4);
  text-align: center;
}

.chat-drawer__empty-title {
  color: var(--color-text);
  font-weight: 500;
}

.chat-drawer__empty-sub {
  font-size: var(--font-size-sm);
  color: var(--color-text-muted);
}

/* ---- 单聊头 ---- */
.chat-drawer__peer-avatar {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  object-fit: cover;
  flex-shrink: 0;
}

.chat-drawer__peer-name {
  flex: 1;
  min-width: 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  font-size: var(--font-size-base);
  font-weight: 500;
}

/* ---- 消息流 ---- */
.chat-drawer__msgs {
  flex: 1;
  overflow-y: auto;
  padding: var(--space-3);
  display: flex;
  flex-direction: column;
  gap: var(--space-3);
}

.chat-drawer__hint {
  margin: auto;
  color: var(--color-text-muted);
  font-size: var(--font-size-sm);
}

.chat-msg {
  display: flex;
  flex-direction: column;
  max-width: 78%;
}

.chat-msg--theirs {
  align-self: flex-start;
  align-items: flex-start;
}

.chat-msg--mine {
  align-self: flex-end;
  align-items: flex-end;
}

.chat-msg__bubble {
  padding: var(--space-2) var(--space-3);
  border-radius: var(--radius-lg);
  font-size: var(--font-size-base);
  line-height: 1.5;
  word-break: break-word;
  white-space: pre-wrap;
}

.chat-msg--theirs .chat-msg__bubble {
  background-color: var(--color-bg-sidebar);
  color: var(--color-text);
}

.chat-msg--mine .chat-msg__bubble {
  background-color: var(--color-primary);
  color: #ffffff;
}

.chat-msg__time {
  margin-top: 2px;
  font-size: 11px;
  color: var(--color-text-muted);
}

/* ---- 输入区 ---- */
.chat-drawer__inputbar {
  flex-shrink: 0;
  padding: var(--space-2) var(--space-3) var(--space-3);
  border-top: 1px solid var(--color-border);
}

.chat-drawer__status {
  padding-bottom: var(--space-1);
  font-size: var(--font-size-sm);
  color: var(--color-text-muted);
}

.chat-drawer__inputrow {
  display: flex;
  align-items: center;
  gap: var(--space-2);
}

.chat-drawer__input {
  flex: 1;
  min-width: 0;
  padding: var(--space-2) var(--space-3);
  border: 1px solid var(--color-border-strong);
  border-radius: 999px;
  background-color: var(--color-bg-sidebar);
  color: var(--color-text);
  outline: none;
  transition: border-color var(--transition-fast);
}

.chat-drawer__input:focus {
  border-color: var(--color-primary);
}

.chat-drawer__send {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 40px;
  height: 40px;
  border-radius: 50%;
  background-color: var(--color-primary);
  color: #ffffff;
  flex-shrink: 0;
  transition: background-color var(--transition-fast);
}

.chat-drawer__send:hover {
  background-color: var(--color-primary-hover);
}

/* ---- 进出动画 ---- */
.chat-layer-enter-active,
.chat-layer-leave-active {
  transition: opacity 0.25s ease;
}

.chat-layer-enter-active .chat-drawer,
.chat-layer-leave-active .chat-drawer {
  transition: transform 0.25s ease;
}

.chat-layer-enter-from,
.chat-layer-leave-to {
  opacity: 0;
}

.chat-layer-enter-from .chat-drawer,
.chat-layer-leave-to .chat-drawer {
  transform: translateX(-100%);
}
</style>
