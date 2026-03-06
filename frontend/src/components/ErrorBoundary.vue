<template>
  <div v-if="error" class="error-boundary">
    <p class="error-title">页面加载异常</p>
    <p class="error-desc">{{ errorMessage }}</p>
    <el-button type="primary" @click="retry">重新加载</el-button>
  </div>
  <slot v-else />
</template>

<script setup>
import { ref, onErrorCaptured } from 'vue'

const error = ref(null)
const errorMessage = ref('')

onErrorCaptured((err, instance, info) => {
  error.value = err
  const msg = err?.message ?? err?.msg
  errorMessage.value = msg || (err && typeof err === 'object' ? (err.response?.data?.message || JSON.stringify(err).slice(0, 200)) : String(err))
  console.error('[ErrorBoundary]', err, info)
  return false
})

function retry() {
  error.value = null
  errorMessage.value = ''
  window.location.reload()
}
</script>

<style scoped>
.error-boundary {
  padding: 48px 24px;
  text-align: center;
  color: #000;
}
.error-title { font-size: 1rem; font-weight: 500; margin-bottom: 8px; }
.error-desc { font-size: 0.875rem; color: #262626; margin-bottom: 16px; }
</style>
