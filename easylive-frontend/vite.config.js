import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import { fileURLToPath, URL } from 'node:url'

// https://vitejs.dev/config/
export default defineConfig({
  plugins: [vue()],
  resolve: {
    alias: {
      // 路径别名：@ 指向 src，避免深层相对路径
      '@': fileURLToPath(new URL('./src', import.meta.url))
    }
  },
  define: {
    // sockjs-client 的产物引用了 Node 的 global，浏览器没有。不替换的话
    // 它一 import 就抛 "global is not defined"，把整个应用打成白屏。
    global: 'globalThis'
  },
  server: {
    // host: true 允许局域网内其他设备通过本机 IP 访问（手机调试等）
    host: true,
    port: 5173,
    open: false,
    proxy: {
      // 开发环境：/api 开头的请求转发到后端 easylive-web
      // ws:true 让 WebSocket 升级请求也能转发（SockJS 的 ws 传输），不配也能用但会退化成轮询
      '/api': {
        target: 'http://localhost:8081',
        changeOrigin: true,
        ws: true
      }
    }
  }
})
