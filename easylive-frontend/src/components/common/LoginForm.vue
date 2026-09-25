<script setup>
import { reactive, ref } from 'vue'
import { useAuth } from '@/composables/useAuth.js'
import '@/styles/form.css'

const emit = defineEmits(['switch'])

const { login } = useAuth()

// 常见邮箱格式：xxx@域名.后缀（不求百分百严谨，够挡住明显填错的）
const EMAIL_REG = /^[\w.+-]+@[\w-]+(\.[\w-]+)+$/

const form = reactive({
  email: '',
  password: ''
})
const errorMessage = ref('')
const submitting = ref(false)

async function handleSubmit() {
  errorMessage.value = ''
  if (!EMAIL_REG.test(form.email)) {
    errorMessage.value = '请输入正确的邮箱地址'
    return
  }
  if (!form.password) {
    errorMessage.value = '请输入密码'
    return
  }
  submitting.value = true
  try {
    await login({ email: form.email, password: form.password })
    form.email = ''
    form.password = ''
  } catch (err) {
    // 后端返回 code=1002 等业务失败时，展示后端给出的 message
    errorMessage.value = err?.message || '登录失败，请重试'
  } finally {
    submitting.value = false
  }
}
</script>

<template>
  <form class="login-form" @submit.prevent="handleSubmit">
    <label class="login-form__field">
      <span class="login-form__label">邮箱</span>
      <input
        v-model="form.email"
        class="login-form__input"
        type="email"
        placeholder="请输入邮箱"
        autocomplete="email"
      />
    </label>

    <label class="login-form__field">
      <span class="login-form__label">密码</span>
      <input
        v-model="form.password"
        class="login-form__input"
        type="password"
        placeholder="请输入密码"
        autocomplete="current-password"
      />
    </label>

    <p v-if="errorMessage" class="login-form__error">{{ errorMessage }}</p>

    <button class="login-form__submit" type="submit" :disabled="submitting">
      {{ submitting ? '登录中...' : '登录' }}
    </button>

    <div class="login-form__footer">
      <span>还没有账号？</span>
      <button class="login-form__link" type="button" @click="emit('switch', 'register')">去注册</button>
    </div>
  </form>
</template>
