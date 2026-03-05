import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import legacy from '@vitejs/plugin-legacy'
import { resolve } from 'path'

export default defineConfig({
  plugins: [
    vue(),
    legacy({
      targets: ['last 2 versions', 'not dead', 'Chrome >= 60', 'Firefox >= 60', 'Safari >= 12', 'Edge >= 79'],
      modernPolyfills: true,
      renderLegacyChunks: true
    })
  ],
  build: {
    cssTarget: 'chrome80'
  },
  resolve: {
    alias: { '@': resolve(__dirname, 'src') }
  },
  server: {
    host: true,
    port: 5173,
    proxy: {
      '/api': { target: 'http://localhost:8080', changeOrigin: true }
    }
  }
})
