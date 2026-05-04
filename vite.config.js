import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import { resolve } from 'path'

export default defineConfig({
  plugins: [vue()],
  root: 'src/frontend',
  resolve: {
    alias: {
      '@': resolve(__dirname, 'src/frontend/src')
    }
  },
  base: './',
  build: {
    outDir: resolve(__dirname, 'src/main/resources/static/dist'),
    assetsDir: 'assets',
    emptyOutDir: true
  },
  server: {
    port: 5173,
    proxy: {
      '/api': {
        target: 'http://localhost:18080',
        changeOrigin: true
      }
    }
  }
})
