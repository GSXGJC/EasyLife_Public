/**
 * 首页「推荐 / 热门」页签的共享状态
 * 模块级单例 ref，供顶栏 TopBar 切换、首页 HomeView 读取，
 * 避免跨组件层层传参。
 */
import { ref } from 'vue'

export const HOME_TABS = [
  { key: 'recommend', label: '推荐' },
  { key: 'hot', label: '热门' }
]

const activeTab = ref(HOME_TABS[0].key)

export function useHomeTabs() {
  function setActiveTab(key) {
    activeTab.value = key
  }

  return {
    HOME_TABS,
    activeTab,
    setActiveTab
  }
}
