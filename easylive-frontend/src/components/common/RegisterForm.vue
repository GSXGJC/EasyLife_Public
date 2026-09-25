<script setup>
import { onBeforeUnmount, reactive, ref } from 'vue'
import { sendEmailCode } from '@/api/auth.js'
import { useAuth } from '@/composables/useAuth.js'
import '@/styles/form.css'

const emit = defineEmits(['switch'])

const { register } = useAuth()

// 常见邮箱格式：xxx@域名.后缀（不求百分百严谨，够挡住明显填错的）
const EMAIL_REG = /^[\w.+-]+@[\w-]+(\.[\w-]+)+$/

const form = reactive({
  nickName: '',
  email: '',
  code: '',
  password: '',
  confirmPassword: ''
})
const errorMessage = ref('')
const sentMessage = ref('') // 发码成功后的提示（验证码真的发到邮箱了，界面上不再模拟显示）
const submitting = ref(false)

// ---- 发送邮箱验证码 ----
const countdown = ref(0) // 重新获取倒计时（秒）
let timer = null

function canSendCode() {
  return countdown.value === 0 && EMAIL_REG.test(form.email)
}

async function sendCode() {
  errorMessage.value = ''
  sentMessage.value = ''
  if (!EMAIL_REG.test(form.email)) {
    errorMessage.value = '请先填写正确的邮箱地址'
    return
  }
  try {
    // 真实接口：后端写 redis 并异步发信；验证码去邮箱里收
    await sendEmailCode(form.email)
    sentMessage.value = '验证码已发送到邮箱，请注意查收'
    countdown.value = 60
    timer = setInterval(() => {
      countdown.value -= 1
      if (countdown.value <= 0) {
        clearInterval(timer)
        timer = null
      }
    }, 1000)
  } catch (err) {
    errorMessage.value = err?.message || '验证码发送失败，请重试'
  }
}

async function handleSubmit() {
  errorMessage.value = ''
  if (!form.nickName) {
    errorMessage.value = '请输入昵称'
    return
  }
  if (!EMAIL_REG.test(form.email)) {
    errorMessage.value = '请输入正确的邮箱地址'
    return
  }
  if (!/^\d{6}$/.test(form.code)) {
    errorMessage.value = '请输入邮箱收到的 6 位验证码'
    return
  }
  if (form.password.length < 6) {
    errorMessage.value = '密码至少 6 位'
    return
  }
  if (form.password !== form.confirmPassword) {
    errorMessage.value = '两次输入的密码不一致'
    return
  }
  submitting.value = true
  try {
    // 调真实注册接口；成功后 useAuth 内部自动登录并关闭弹窗
    await register({ email: form.email, vertifiCode: form.code, nickName: form.nickName, password: form.password })
    // 清空表单（弹窗已关闭，这里兜底）
    form.nickName = ''
    form.email = ''
    form.code = ''
    form.password = ''
    form.confirmPassword = ''
    sentMessage.value = ''
  } catch (err) {
    // 后端返回 code=非200（如验证码错、邮箱已注册）时，展示后端 message
    errorMessage.value = err?.message || '注册失败，请重试'
  } finally {
    submitting.value = false
  }
}

onBeforeUnmount(() => {
  if (timer) clearInterval(timer)
})
</script>

<template>
  <form class="login-form" @submit.prevent="handleSubmit">
    <label class="login-form__field">
      <span class="login-form__label">昵称</span>
      <input
        v-model="form.nickName"
        class="login-form__input"
        type="text"
        maxlength="20"
        placeholder="请输入昵称"
        autocomplete="nickname"
      />
    </label>

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

    <div class="login-form__field">
      <span class="login-form__label">验证码</span>
      <div class="login-form__code-row">
        <input
          v-model="form.code"
          class="login-form__input"
          type="text"
          inputmode="numeric"
          maxlength="6"
          placeholder="6 位验证码"
        />
        <button
          class="login-form__code-btn"
          type="button"
          :disabled="!canSendCode()"
          @click="sendCode"
        >
          {{ countdown > 0 ? `${countdown}s 后重发` : '获取验证码' }}
        </button>
      </div>
    </div>

    <!-- 发码成功提示：验证码真的发到邮箱，去邮箱收 -->
    <p v-if="sentMessage" class="login-form__sms-hint">{{ sentMessage }}</p>

    <label class="login-form__field">
      <span class="login-form__label">密码</span>
      <input
        v-model="form.password"
        class="login-form__input"
        type="password"
        placeholder="至少 6 位"
        autocomplete="new-password"
      />
    </label>

    <label class="login-form__field">
      <span class="login-form__label">确认密码</span>
      <input
        v-model="form.confirmPassword"
        class="login-form__input"
        type="password"
        placeholder="再次输入密码"
        autocomplete="new-password"
      />
    </label>

    <p v-if="errorMessage" class="login-form__error">{{ errorMessage }}</p>

    <button class="login-form__submit" type="submit" :disabled="submitting">
      {{ submitting ? '注册中...' : '注册并登录' }}
    </button>

    <div class="login-form__footer">
      <span>已有账号？</span>
      <button class="login-form__link" type="button" @click="emit('switch', 'login')">去登录</button>
    </div>
  </form>
</template>
