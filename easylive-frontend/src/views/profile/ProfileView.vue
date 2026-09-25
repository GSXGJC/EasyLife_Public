<script setup>
import { computed, onMounted, reactive, ref, watch } from 'vue'
import { useAuth } from '@/composables/useAuth.js'
import { useProfile } from '@/composables/useProfile.js'
import VideoGrid from '@/components/video/VideoGrid.vue'

const { isLoggedIn, user, userName, showLogin, logout } = useAuth()
const {
  profile,
  updateProfile,
  myVideos,
  likedVideos,
  collectedVideos,
  coinedVideos,
  loading,
  loadFailed,
  loadProfileVideos
} = useProfile()

// —— 基本信息展示（昵称/邮箱来自登录信息，其余来自本地编辑的资料） ——
const SEX_LABELS = ['男', '女', '未知']

// 昵称只认登录账号（user.name，登录接口从数据库拿的那份）；
// 不再合并本地 profile.nickName——本地"假昵称"曾盖住真名、还串到下个账号。
const display = computed(() => ({
  nickName: userName.value,
  avatar: profile.value.avatar || '',
  sex: profile.value.sex ?? 0,
  birthday: profile.value.birthday || '',
  school: profile.value.school || '',
  personalIntroduction: profile.value.personalIntroduction || '这个人很懒，什么都没写。'
}))

/** 邮箱脱敏：abc@xx.com → a***@xx.com（只留首个字符 + @ 后的域名） */
function maskEmail(email) {
  const s = String(email || '')
  const at = s.indexOf('@')
  if (at <= 0) return s ? `${s.slice(0, 1)}***` : ''
  return `${s.slice(0, 1)}***${s.slice(at)}`
}

// —— 编辑资料（内联切换表单） ——
// 表单不含昵称：后端还没有"改昵称"接口，本地改了也不入库，
// 反而会盖住真名/串号，所以昵称暂时只读（展示在卡片上）。
const editing = ref(false)
const editForm = reactive({
  avatar: '',
  sex: 0,
  birthday: '',
  school: '',
  personalIntroduction: ''
})

function openEdit() {
  Object.assign(editForm, {
    avatar: display.value.avatar,
    sex: display.value.sex,
    birthday: display.value.birthday,
    school: display.value.school,
    personalIntroduction: display.value.personalIntroduction
  })
  editing.value = true
}

function saveEdit() {
  updateProfile({ ...editForm })
  editing.value = false
}

// —— 四个 tab ——
// 用 computed 包一层：myVideos/likedVideos 等都是 ref，异步加载完成后 .value 变化，
// computed 会跟着重算，页面才刷新；直接在外面用 ref 对象不会自动解包。
const tabs = computed(() => [
  { key: 'upload', label: '我的投稿', videos: myVideos.value },
  { key: 'like', label: '点赞过的', videos: likedVideos.value },
  { key: 'collect', label: '收藏过的', videos: collectedVideos.value },
  { key: 'coin', label: '投过币的', videos: coinedVideos.value }
])
const activeTab = ref('upload')
const currentTab = computed(() => tabs.value.find((t) => t.key === activeTab.value))

// 进来时已登录就拉一次列表；登录/切换账号后重新拉，退出则回到第一个 tab
onMounted(() => {
  if (isLoggedIn.value) loadProfileVideos()
})
watch(isLoggedIn, (val) => {
  if (val) {
    loadProfileVideos()
  } else {
    activeTab.value = 'upload'
  }
})

async function onLogout() {
  if (window.confirm('确定要退出登录吗？')) {
    await logout()
  }
}
</script>

<template>
  <div class="profile-view">
    <template v-if="isLoggedIn">
      <!-- 用户信息卡 -->
      <div class="profile-view__card">
        <div class="profile-view__avatar">
          <img v-if="display.avatar" :src="display.avatar" alt="头像" />
          <span v-else>{{ display.nickName.charAt(0).toUpperCase() }}</span>
        </div>

        <div class="profile-view__info">
          <p class="profile-view__name">{{ display.nickName }}</p>
          <p class="profile-view__meta">
            <template v-if="maskEmail(user?.email)">邮箱 {{ maskEmail(user?.email) }}</template>
            <template v-if="display.birthday"> · 生日 {{ display.birthday }}</template>
            <template v-if="display.school"> · {{ display.school }}</template>
            · 性别 {{ SEX_LABELS[display.sex] }}
          </p>
          <p class="profile-view__intro">{{ display.personalIntroduction }}</p>
        </div>

        <div class="profile-view__actions">
          <button class="profile-view__edit" type="button" @click="openEdit">编辑资料</button>
          <button class="profile-view__logout" type="button" @click="onLogout">退出登录</button>
        </div>
      </div>

      <!-- 编辑资料表单 -->
      <div v-if="editing" class="profile-view__edit-form">
        <h3 class="profile-view__edit-title">编辑资料</h3>

        <!-- 昵称不在这里改：后端没做改昵称接口，本地改了不生效还会串号；先只读展示在卡片上 -->
        <label class="profile-view__field">
          <span>头像地址</span>
          <input v-model="editForm.avatar" type="text" placeholder="图片 URL（选填）" />
        </label>
        <label class="profile-view__field">
          <span>性别</span>
          <select v-model.number="editForm.sex">
            <option v-for="(label, i) in SEX_LABELS" :key="i" :value="i">{{ label }}</option>
          </select>
        </label>
        <label class="profile-view__field">
          <span>生日</span>
          <input v-model="editForm.birthday" type="date" />
        </label>
        <label class="profile-view__field">
          <span>学校</span>
          <input v-model="editForm.school" type="text" maxlength="45" />
        </label>
        <label class="profile-view__field">
          <span>个人简介</span>
          <textarea v-model="editForm.personalIntroduction" rows="3" maxlength="200"></textarea>
        </label>

        <div class="profile-view__edit-actions">
          <button class="profile-view__save" type="button" @click="saveEdit">保存</button>
          <button class="profile-view__cancel" type="button" @click="editing = false">取消</button>
        </div>
      </div>

      <!-- 视频 tab -->
      <div class="profile-view__tabs">
        <button
          v-for="t in tabs"
          :key="t.key"
          type="button"
          class="profile-view__tab"
          :class="{ 'profile-view__tab--active': activeTab === t.key }"
          @click="activeTab = t.key"
        >
          {{ t.label }}
        </button>
      </div>

      <!-- 列表区：四个 tab 都走真实接口，加载中 / 失败可重试 / 空 / 有数据四态 -->
      <div class="profile-view__tab-body">
        <p v-if="loading" class="profile-view__empty">加载中...</p>
        <div v-else-if="loadFailed" class="profile-view__empty">
          <p>加载失败，请检查网络后重试</p>
          <button class="profile-view__retry" type="button" @click="loadProfileVideos">重试</button>
        </div>
        <p v-else-if="currentTab.videos.length === 0" class="profile-view__empty">
          这里还什么都没有，去首页逛逛吧
        </p>
        <VideoGrid v-else :videos="currentTab.videos" />
      </div>
    </template>

    <!-- 未登录 -->
    <template v-else>
      <div class="placeholder-view">
        <h1>我的</h1>
        <p>登录后查看你的收藏、历史与投稿</p>
        <button class="placeholder-view__btn" type="button" @click="showLogin">立即登录</button>
      </div>
    </template>
  </div>
</template>

<style scoped>
.profile-view {
  max-width: 1200px;
  margin: 0 auto;
}

/* 用户信息卡 */
.profile-view__card {
  display: flex;
  align-items: flex-start;
  gap: var(--space-4);
  padding: var(--space-5);
  border-radius: var(--radius-lg);
  background-color: var(--color-bg-elevated);
  box-shadow: var(--shadow-card);
}

.profile-view__avatar {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 72px;
  height: 72px;
  border-radius: 50%;
  background: linear-gradient(135deg, var(--color-primary), var(--color-primary-hover));
  color: #ffffff;
  font-size: 28px;
  font-weight: 600;
  overflow: hidden;
  flex-shrink: 0;
}

.profile-view__avatar img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.profile-view__info {
  flex: 1;
  min-width: 0;
}

.profile-view__name {
  font-size: var(--font-size-title);
  font-weight: 600;
}

.profile-view__meta {
  margin-top: var(--space-1);
  font-size: var(--font-size-sm);
  color: var(--color-text-secondary);
}

.profile-view__intro {
  margin-top: var(--space-2);
  font-size: var(--font-size-base);
  color: var(--color-text-secondary);
  line-height: 1.6;
}

.profile-view__actions {
  display: flex;
  flex-direction: column;
  gap: var(--space-2);
  flex-shrink: 0;
}

.profile-view__edit,
.profile-view__logout {
  padding: var(--space-2) var(--space-4);
  border-radius: 999px;
  font-size: var(--font-size-sm);
  transition: all var(--transition-fast);
}

.profile-view__edit {
  background-color: var(--color-primary);
  color: #ffffff;
}

.profile-view__edit:hover {
  background-color: var(--color-primary-hover);
}

.profile-view__logout {
  border: 1px solid var(--color-border-strong);
  color: var(--color-text-secondary);
}

.profile-view__logout:hover {
  border-color: var(--color-danger);
  color: var(--color-danger);
  background-color: var(--color-bg-hover);
}

/* 编辑资料表单 */
.profile-view__edit-form {
  margin-top: var(--space-4);
  padding: var(--space-5);
  border-radius: var(--radius-lg);
  background-color: var(--color-bg-elevated);
  box-shadow: var(--shadow-card);
}

.profile-view__edit-title {
  margin-bottom: var(--space-4);
  font-size: var(--font-size-lg);
  font-weight: 600;
}

.profile-view__field {
  display: flex;
  flex-direction: column;
  gap: var(--space-1);
  margin-bottom: var(--space-3);
}

.profile-view__field span {
  font-size: var(--font-size-sm);
  color: var(--color-text-secondary);
}

.profile-view__field input,
.profile-view__field select,
.profile-view__field textarea {
  width: 100%;
  padding: var(--space-2) var(--space-3);
  border: 1px solid var(--color-border-strong);
  border-radius: var(--radius-md);
  background-color: var(--color-bg);
  color: var(--color-text);
}

.profile-view__field input:focus,
.profile-view__field select:focus,
.profile-view__field textarea:focus {
  border-color: var(--color-primary);
  outline: none;
}

.profile-view__edit-actions {
  display: flex;
  gap: var(--space-2);
  margin-top: var(--space-4);
}

.profile-view__save,
.profile-view__cancel {
  padding: var(--space-2) var(--space-5);
  border-radius: 999px;
  font-weight: 500;
}

.profile-view__save {
  background-color: var(--color-primary);
  color: #ffffff;
}

.profile-view__save:hover {
  background-color: var(--color-primary-hover);
}

.profile-view__cancel {
  border: 1px solid var(--color-border-strong);
  color: var(--color-text-secondary);
}

.profile-view__cancel:hover {
  background-color: var(--color-bg-hover);
}

/* 视频 tab */
.profile-view__tabs {
  display: flex;
  gap: var(--space-2);
  margin: var(--space-5) 0 var(--space-4);
  border-bottom: 1px solid var(--color-border);
}

.profile-view__tab {
  padding: var(--space-2) var(--space-4);
  color: var(--color-text-secondary);
  font-weight: 500;
  border-bottom: 2px solid transparent;
  transition: color var(--transition-fast);
}

.profile-view__tab:hover {
  color: var(--color-text);
}

.profile-view__tab--active {
  color: var(--color-primary);
  border-bottom-color: var(--color-primary);
}

/* 列表三态提示 */
.profile-view__tab-body {
  min-height: 120px;
}

.profile-view__empty {
  padding: var(--space-6) 0;
  text-align: center;
  color: var(--color-text-muted);
  font-size: var(--font-size-base);
}

.profile-view__retry {
  display: inline-block;
  margin-top: var(--space-2);
  padding: var(--space-1) var(--space-4);
  border: 1px solid var(--color-primary);
  border-radius: 999px;
  color: var(--color-primary);
  transition: background-color var(--transition-fast), color var(--transition-fast);
}

.profile-view__retry:hover {
  background-color: var(--color-primary);
  color: #ffffff;
}

.placeholder-view {
  padding: var(--space-6) 0;
  text-align: center;
  color: var(--color-text-secondary);
}

.placeholder-view h1 {
  font-size: var(--font-size-title);
  margin-bottom: var(--space-3);
}

.placeholder-view p {
  margin-bottom: var(--space-4);
}

.placeholder-view__btn {
  padding: var(--space-2) var(--space-6);
  border-radius: 999px;
  background-color: var(--color-primary);
  color: #ffffff;
  font-weight: 500;
  transition: background-color var(--transition-fast);
}

.placeholder-view__btn:hover {
  background-color: var(--color-primary-hover);
}
</style>
