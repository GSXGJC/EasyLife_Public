<script setup>
import { ref } from 'vue'
import BaseIcon from '@/components/common/BaseIcon.vue'
import { useHomeTabs } from '@/composables/useHomeTabs.js'

const { HOME_TABS, activeTab, setActiveTab } = useHomeTabs()
const keyword = ref('')

function onSearch() {
  // TODO: 跳转搜索结果页
  console.log('TODO: 搜索关键词 =', keyword.value)
}
</script>

<template>
  <header class="topbar">
    <!-- 左：推荐 / 热门 页签 -->
    <div class="topbar__tabs" role="tablist">
      <button
        v-for="tab in HOME_TABS"
        :key="tab.key"
        type="button"
        role="tab"
        class="topbar__tab"
        :class="{ 'topbar__tab--active': activeTab === tab.key }"
        @click="setActiveTab(tab.key)"
      >
        {{ tab.label }}
      </button>
    </div>

    <!-- 右：搜索框 -->
    <form class="topbar__search" role="search" @submit.prevent="onSearch">
      <BaseIcon name="search" :size="18" />
      <input
        v-model="keyword"
        class="topbar__search-input"
        type="text"
        placeholder="搜索你感兴趣的视频"
      />
    </form>
  </header>
</template>

<style scoped>
.topbar {
  height: var(--topbar-height);
  flex-shrink: 0;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: var(--space-5);
  padding: 0 var(--space-6);
  background-color: var(--color-bg);
  border-bottom: 1px solid var(--color-border);
}

.topbar__tabs {
  display: flex;
  align-items: center;
  gap: var(--space-1);
}

.topbar__tab {
  padding: var(--space-2) var(--space-4);
  border-radius: var(--radius-md);
  color: var(--color-text-secondary);
  font-weight: 500;
  transition: background-color var(--transition-fast), color var(--transition-fast);
}

.topbar__tab:hover {
  background-color: var(--color-bg-hover);
  color: var(--color-text);
}

.topbar__tab--active {
  color: var(--color-primary);
  background-color: var(--color-bg-hover);
}

.topbar__search {
  display: flex;
  align-items: center;
  gap: var(--space-2);
  width: 320px;
  max-width: 40%;
  padding: var(--space-2) var(--space-3);
  background-color: var(--color-bg-sidebar);
  border: 1px solid transparent;
  border-radius: 999px;
  color: var(--color-text-muted);
  transition: border-color var(--transition-fast);
}

.topbar__search:focus-within {
  border-color: var(--color-primary);
}

.topbar__search-input {
  flex: 1;
  min-width: 0;
  background: transparent;
  border: none;
  outline: none;
}

.topbar__search-input::placeholder {
  color: var(--color-text-muted);
}
</style>
