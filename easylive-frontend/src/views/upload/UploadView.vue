<script setup>
import { onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { useAuth } from '@/composables/useAuth.js'
import { publishVideo } from '@/api/video.js'
import { formatDuration } from '@/utils/format.js'

const router = useRouter()
const { isLoggedIn, showLogin } = useAuth()

const CATEGORIES = [
  { value: 0, label: '未分类' },
  { value: 1, label: '科技' },
  { value: 2, label: '生活' },
  { value: 3, label: '游戏' },
  { value: 4, label: '音乐' }
]

const form = reactive({
  title: '',
  description: '',
  category: 0,
  coverFile: null,
  videoFile: null,
  duration: 0
})
const errorMessage = ref('')
const submitting = ref(false)

/** 上传上限，必须和 application.yaml 的 spring.servlet.multipart.max-file-size 一致（100MB） */
const MAX_FILE_SIZE = 100 * 1024 * 1024 // 100MB，单位字节

/** 封面图片上限，必须和后端 VideoServiceImpl 里的检查一致（5MB） */
const MAX_IMAGE_SIZE = 5 * 1024 * 1024 // 5MB

// 未登录直接访问上传页：弹登录框并回首页
onMounted(() => {
  if (!isLoggedIn.value) {
    showLogin()
    router.replace('/home')
  }
})

/** 去掉扩展名，如 "demo.mp4" -> "demo" */
function nameWithoutExt(name) {
  const dot = name.lastIndexOf('.')
  return dot > 0 ? name.slice(0, dot) : name
}

/** 选视频后：标题默认用文件名（可再改）、自动读时长（秒） */
function onVideoChange(event) {
  const file = event.target.files?.[0] || null
  form.videoFile = file
  if (!file) return
  // 超过上限：不发请求，直接提示（后端超过上限会掐连接，连 Result 都拿不到）
  if (file.size > MAX_FILE_SIZE) {
    errorMessage.value = '视频文件超过 100MB，无法上传'
    form.videoFile = null
    return
  }
  errorMessage.value = ''
  form.title = nameWithoutExt(file.name)
  readDuration(file).then((seconds) => {
    form.duration = seconds
  })
}

/**
 * 用隐藏的 <video> 元素读取本地视频时长。
 * URL.createObjectURL 生成临时地址给 video 加载元数据，读完释放。
 */
function readDuration(file) {
  return new Promise((resolve) => {
    const url = URL.createObjectURL(file)
    const el = document.createElement('video')
    el.preload = 'metadata'
    el.onloadedmetadata = () => {
      resolve(Math.round(el.duration || 0))
      URL.revokeObjectURL(url)
    }
    el.onerror = () => {
      resolve(0)
      URL.revokeObjectURL(url)
    }
    el.src = url
  })
}

function onCoverChange(event) {
  const file = event.target.files?.[0] || null
  form.coverFile = file
  if (!file) return
  if (file.size > MAX_IMAGE_SIZE) {
    errorMessage.value = '封面图片超过 5MB，请压缩后再上传'
    form.coverFile = null
    return
  }
  errorMessage.value = ''
}

async function handleSubmit() {
  errorMessage.value = ''
  if (!form.title.trim()) {
    errorMessage.value = '请输入标题'
    return
  }
  if (!form.videoFile) {
    errorMessage.value = '请选择视频文件'
    return
  }
  if (form.videoFile.size > MAX_FILE_SIZE) {
    errorMessage.value = '视频文件超过 100MB，无法上传'
    return
  }
  if (form.coverFile && form.coverFile.size > MAX_IMAGE_SIZE) {
    errorMessage.value = '封面图片超过 5MB，无法上传'
    return
  }

  submitting.value = true
  try {
    const fd = new FormData()
    fd.append('file', form.videoFile)
    if (form.coverFile) fd.append('cover', form.coverFile)
    fd.append('title', form.title.trim())
    fd.append('description', form.description.trim())
    fd.append('category', form.category)
    fd.append('duration', form.duration || 0)
    await publishVideo(fd)
    router.push('/home')
  } catch (err) {
    errorMessage.value = err?.message || '发布失败，请重试'
  } finally {
    submitting.value = false
  }
}
</script>

<template>
  <div class="upload-view">
    <h1 class="upload-view__title">发布视频</h1>

    <form class="upload-form" @submit.prevent="handleSubmit">
      <!-- 视频文件（必选）：放最上面，选完自动填标题 -->
      <div class="upload-form__field">
        <span class="upload-form__label">视频文件 <em>*</em></span>
        <label class="upload-form__file">
          <span>{{ form.videoFile ? form.videoFile.name : '选择视频文件' }}</span>
          <input type="file" accept="video/*" @change="onVideoChange" />
        </label>
        <!-- 时长自动读取，只展示不可改 -->
        <p v-if="form.duration > 0" class="upload-form__hint">视频时长：{{ formatDuration(form.duration) }}</p>
      </div>

      <!-- 标题 -->
      <label class="upload-form__field">
        <span class="upload-form__label">标题 <em>*</em></span>
        <input
          v-model="form.title"
          class="upload-form__input"
          type="text"
          maxlength="100"
          placeholder="起个吸引人的标题"
        />
      </label>

      <!-- 分类 -->
      <label class="upload-form__field">
        <span class="upload-form__label">分类</span>
        <select v-model.number="form.category" class="upload-form__input">
          <option v-for="c in CATEGORIES" :key="c.value" :value="c.value">{{ c.label }}</option>
        </select>
      </label>

      <!-- 简介 -->
      <label class="upload-form__field">
        <span class="upload-form__label">简介</span>
        <textarea
          v-model="form.description"
          class="upload-form__input upload-form__textarea"
          rows="4"
          maxlength="500"
          placeholder="介绍一下这个视频（选填）"
        ></textarea>
      </label>

      <!-- 封面（可选） -->
      <div class="upload-form__field">
        <span class="upload-form__label">封面</span>
        <label class="upload-form__file">
          <span>{{ form.coverFile ? form.coverFile.name : '选择封面图片（选填）' }}</span>
          <input type="file" accept="image/*" @change="onCoverChange" />
        </label>
      </div>

      <p v-if="errorMessage" class="upload-form__error">{{ errorMessage }}</p>

      <button class="upload-form__submit" type="submit" :disabled="submitting">
        {{ submitting ? '发布中...' : '发布' }}
      </button>
    </form>
  </div>
</template>

<style scoped>
.upload-view {
  max-width: 720px;
  margin: 0 auto;
}

.upload-view__title {
  margin-bottom: var(--space-5);
  font-size: var(--font-size-title);
  font-weight: 600;
}

.upload-form {
  display: flex;
  flex-direction: column;
  gap: var(--space-4);
}

.upload-form__field {
  display: flex;
  flex-direction: column;
  gap: var(--space-1);
}

.upload-form__label {
  font-size: var(--font-size-sm);
  color: var(--color-text-secondary);
}

.upload-form__label em {
  color: var(--color-danger);
  font-style: normal;
}

.upload-form__input {
  width: 100%;
  padding: var(--space-2) var(--space-3);
  border: 1px solid var(--color-border-strong);
  border-radius: var(--radius-md);
  background-color: var(--color-bg-elevated);
  color: var(--color-text);
  transition: border-color var(--transition-fast);
}

.upload-form__input:focus {
  border-color: var(--color-primary);
  outline: none;
}

.upload-form__textarea {
  resize: vertical;
}

.upload-form__file {
  display: block;
  padding: var(--space-2) var(--space-3);
  border: 1px dashed var(--color-border-strong);
  border-radius: var(--radius-md);
  color: var(--color-text-secondary);
  cursor: pointer;
  transition: border-color var(--transition-fast), color var(--transition-fast);
}

.upload-form__file:hover {
  border-color: var(--color-primary);
  color: var(--color-primary);
}

.upload-form__file input {
  display: none;
}

.upload-form__hint {
  font-size: var(--font-size-sm);
  color: var(--color-text-muted);
}

.upload-form__error {
  color: var(--color-danger);
  font-size: var(--font-size-sm);
}

.upload-form__submit {
  align-self: flex-start;
  padding: var(--space-2) var(--space-6);
  border-radius: 999px;
  background-color: var(--color-primary);
  color: #ffffff;
  font-weight: 500;
  transition: background-color var(--transition-fast);
}

.upload-form__submit:hover {
  background-color: var(--color-primary-hover);
}

.upload-form__submit:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}
</style>
