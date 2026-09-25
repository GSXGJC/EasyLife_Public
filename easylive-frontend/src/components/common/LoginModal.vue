<script setup>
import { onBeforeUnmount, onMounted, ref, watch } from 'vue'
import BaseIcon from '@/components/common/BaseIcon.vue'
import LoginForm from '@/components/common/LoginForm.vue'
import RegisterForm from '@/components/common/RegisterForm.vue'
import { useAuth } from '@/composables/useAuth.js'

const { loginVisible, hideLogin } = useAuth()

const mode = ref('login') // 'login' | 'register'

// 每次打开弹窗默认回到「登录」
watch(loginVisible, (visible) => {
  if (visible) mode.value = 'login'
})

function switchMode(next) {
  mode.value = next
}

function onKeydown(event) {
  if (event.key === 'Escape' && loginVisible.value) hideLogin()
}

onMounted(() => window.addEventListener('keydown', onKeydown))
onBeforeUnmount(() => window.removeEventListener('keydown', onKeydown))
</script>

<template>
  <Teleport to="body">
    <Transition name="login-modal">
      <div v-if="loginVisible" class="login-modal" @mousedown.self="hideLogin">
        <div class="login-modal__card" role="dialog" aria-modal="true" aria-label="登录 / 注册">
          <button class="login-modal__close" type="button" title="关闭" @click="hideLogin">
            <BaseIcon name="x" :size="18" />
          </button>

          <h2 class="login-modal__title">easylive</h2>
          <p class="login-modal__subtitle">轻松看视频，发现更多精彩</p>

          <!-- 登录 / 注册 切换 -->
          <div class="login-modal__tabs" role="tablist">
            <button
              type="button"
              role="tab"
              class="login-modal__tab"
              :class="{ 'login-modal__tab--active': mode === 'login' }"
              @click="switchMode('login')"
            >
              登录
            </button>
            <button
              type="button"
              role="tab"
              class="login-modal__tab"
              :class="{ 'login-modal__tab--active': mode === 'register' }"
              @click="switchMode('register')"
            >
              注册
            </button>
          </div>

          <LoginForm v-if="mode === 'login'" @switch="switchMode" />
          <RegisterForm v-else @switch="switchMode" />
        </div>
      </div>
    </Transition>
  </Teleport>
</template>

<style scoped>
.login-modal {
  position: fixed;
  inset: 0;
  z-index: 1000;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: var(--space-4);
  background-color: rgba(0, 0, 0, 0.5);
}

.login-modal__card {
  position: relative;
  width: 100%;
  max-width: 400px;
  padding: var(--space-6);
  border-radius: var(--radius-lg);
  background-color: var(--color-bg);
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.2);
}

.login-modal__close {
  position: absolute;
  top: var(--space-3);
  right: var(--space-3);
  display: flex;
  align-items: center;
  justify-content: center;
  width: 32px;
  height: 32px;
  border-radius: 50%;
  color: var(--color-text-muted);
  transition: background-color var(--transition-fast), color var(--transition-fast);
}

.login-modal__close:hover {
  background-color: var(--color-bg-hover);
  color: var(--color-text);
}

.login-modal__title {
  font-size: 24px;
  font-weight: 600;
}

.login-modal__subtitle {
  margin-top: var(--space-1);
  font-size: var(--font-size-sm);
  color: var(--color-text-secondary);
}

/* 登录 / 注册 分段切换 */
.login-modal__tabs {
  display: flex;
  gap: var(--space-1);
  margin-top: var(--space-4);
  padding: var(--space-1);
  border-radius: 999px;
  background-color: var(--color-bg-sidebar);
}

.login-modal__tab {
  flex: 1;
  padding: var(--space-2);
  border-radius: 999px;
  color: var(--color-text-secondary);
  font-weight: 500;
  transition: background-color var(--transition-fast), color var(--transition-fast);
}

.login-modal__tab--active {
  background-color: var(--color-bg);
  color: var(--color-primary);
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.1);
}

/* 弹窗过渡动画 */
.login-modal-enter-active,
.login-modal-leave-active {
  transition: opacity 0.25s ease;
}

.login-modal-enter-active .login-modal__card,
.login-modal-leave-active .login-modal__card {
  transition: transform 0.25s ease;
}

.login-modal-enter-from,
.login-modal-leave-to {
  opacity: 0;
}

.login-modal-enter-from .login-modal__card,
.login-modal-leave-to .login-modal__card {
  transform: translateY(12px) scale(0.98);
}
</style>
