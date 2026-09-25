# easylive-frontend

easylive 视频网站前端（类似 bilibili），基于 **Vue 3 + Vite + vue-router**。

## 环境要求

- Node.js ≥ 18
- npm ≥ 9

## 快速开始

```bash
npm install      
npm run dev      
npm run build    
npm run preview  
```

## 目录结构

```
src/
├── api/              # 接口层（预留，后续对接后端 easylive-web）
├── assets/           # 静态资源（icons 图标、images 占位图）
├── components/
│   ├── common/       # 通用组件（BaseIcon 等）
│   ├── layout/       # 布局组件（SiteLayout / SideBar / TopBar）
│   └── video/        # 视频组件（VideoCard / VideoGrid）
├── composables/      # 组合式函数（useTheme 主题、useHomeTabs 推荐/热门）
├── mock/             # 前端 mock 数据（后续替换为后端接口）
├── router/           # 路由配置
├── styles/           # 全局样式（variables.css 主题变量 / global.css）
├── utils/            # 工具函数（格式化等）
└── views/            # 页面（home / feed / profile）
```

## 主题（白 / 暗）

点击左侧栏的主题按钮切换，通过根元素 `data-theme` 属性控制 CSS 变量，
偏好保存在 `localStorage`（key: `easylive-theme`）。

## 待办 / 扩展方向

- [ ] 对接后端接口（替换 `src/api/video.js` 中的 mock 实现）
- [ ] 上传视频页面
- [ ] 视频详情页 / 播放页
- [ ] 搜索功能
- [ ] 动态、我的页面
