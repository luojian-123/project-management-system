<template>
  <div class="login-page">
    <div class="login-bg-grid" />
    <div class="login-bg-glow" />
    <div class="login-bg-line" />
    <div class="login-box">
      <div class="login-header">
        <h1>项目管理系统</h1>
        <p class="login-desc">项目与协作管理平台</p>
      </div>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="0" class="form">
        <el-form-item prop="username">
          <el-input v-model="form.username" placeholder="用户名" size="large" prefix-icon="User" class="input-tech" />
        </el-form-item>
        <el-form-item prop="password">
          <el-input v-model="form.password" type="password" placeholder="密码" size="large" prefix-icon="Lock" show-password class="input-tech" @keyup.enter="onSubmit" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" size="large" class="btn-login" :loading="loading" @click="onSubmit">登录</el-button>
        </el-form-item>
      </el-form>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/store/user'
import { ElMessage } from 'element-plus'

const router = useRouter()
const userStore = useUserStore()
const formRef = ref()
const loading = ref(false)
const form = reactive({ username: '', password: '' })
const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

async function onSubmit() {
  await formRef.value?.validate()
  loading.value = true
  try {
    await userStore.login(form)
    await userStore.fetchInfo()
    ElMessage.success('登录成功')
    router.push('/')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(160deg, #0a0e1a 0%, #0f172a 40%, #0c1222 100%);
  position: relative;
  overflow: hidden;
}
/* 科技网格底纹 */
.login-bg-grid {
  position: absolute;
  inset: 0;
  background-image:
    linear-gradient(rgba(0,212,255,0.06) 1px, transparent 1px),
    linear-gradient(90deg, rgba(0,212,255,0.06) 1px, transparent 1px);
  background-size: 40px 40px;
  pointer-events: none;
}
.login-bg-grid::after {
  content: '';
  position: absolute;
  inset: 0;
  background: linear-gradient(180deg, transparent 0%, rgba(10,14,26,0.5) 50%, #0a0e1a 100%);
  pointer-events: none;
}
/* 炫彩光晕：青 + 紫 + 中央微光 */
.login-bg-glow {
  position: absolute;
  width: 100%;
  height: 100%;
  top: 0;
  left: 0;
  background:
    radial-gradient(ellipse 90% 70% at 15% 20%, rgba(0,212,255,0.18) 0%, transparent 50%),
    radial-gradient(ellipse 80% 90% at 85% 80%, rgba(124,58,237,0.2) 0%, transparent 50%),
    radial-gradient(ellipse 60% 60% at 50% 50%, rgba(168,85,247,0.06) 0%, transparent 65%);
  pointer-events: none;
}
/* 斜向科技线条 */
.login-bg-line {
  position: absolute;
  inset: -50%;
  width: 200%;
  height: 200%;
  background: repeating-linear-gradient(
    105deg,
    transparent,
    transparent 80px,
    rgba(0,212,255,0.04) 80px,
    rgba(0,212,255,0.04) 81px
  );
  pointer-events: none;
  animation: login-line-shift 25s linear infinite;
}
@keyframes login-line-shift {
  0% { transform: translate(0, 0); }
  100% { transform: translate(81px, 0); }
}
/* 登录卡片：简洁白底 + 炫彩描边与投影 */
.login-box {
  width: 420px;
  padding: 48px 44px;
  background: rgba(255,255,255,0.98);
  border-radius: 20px;
  box-shadow:
    0 0 0 1px rgba(255,255,255,0.6),
    0 0 60px -10px rgba(0,212,255,0.2),
    0 0 80px -20px rgba(124,58,237,0.15),
    0 28px 56px -14px rgba(0,0,0,0.35);
  position: relative;
  backdrop-filter: blur(16px);
}
.login-box::before {
  content: '';
  position: absolute;
  inset: -1px;
  border-radius: 21px;
  padding: 1px;
  background: linear-gradient(135deg, rgba(0,212,255,0.6), rgba(124,58,237,0.5), rgba(168,85,247,0.45));
  -webkit-mask: linear-gradient(#fff 0 0) content-box, linear-gradient(#fff 0 0);
  mask: linear-gradient(#fff 0 0) content-box, linear-gradient(#fff 0 0);
  -webkit-mask-composite: xor;
  mask-composite: exclude;
  pointer-events: none;
}
.login-header { text-align: center; margin-bottom: 36px; }
.login-header h1 {
  font-size: 1.85rem;
  font-weight: 700;
  margin-bottom: 10px;
  letter-spacing: 0.04em;
  color: #000;
  background: none;
  -webkit-background-clip: unset;
  -webkit-text-fill-color: #000;
  background-clip: unset;
}
.login-desc { font-size: 0.875rem; color: #262626; letter-spacing: 0.02em; }
.form :deep(.el-form-item) { margin-bottom: 20px; }
.form :deep(.el-form-item:last-child) { margin-bottom: 0; margin-top: 12px; }
/* 登录页输入框：科技感描边，聚焦青紫光晕 */
.form :deep(.input-tech .el-input__wrapper) {
  border-radius: 10px !important;
  min-height: 44px !important;
  height: 44px !important;
  background: #ffffff !important;
  box-shadow: 0 0 0 1px rgba(0,212,255,0.15) !important;
  transition: box-shadow 0.25s ease;
}
.form :deep(.input-tech .el-input__wrapper:hover) {
  box-shadow: 0 0 0 1px rgba(0,212,255,0.3) !important;
}
.form :deep(.input-tech .el-input__wrapper.is-focus) {
  box-shadow: 0 0 0 2px rgba(0,212,255,0.5), 0 0 20px rgba(0,212,255,0.12) !important;
}
.form :deep(.input-tech .el-input__inner::placeholder),
.form :deep(.input-tech input::placeholder) {
  color: #595959 !important;
}
.form :deep(.input-tech .el-input__inner),
.form :deep(.input-tech input) {
  color: #000 !important;
}
.form :deep(.input-tech .el-input__prefix .el-icon) {
  color: #262626 !important;
}
/* 登录按钮：炫彩渐变 + 光晕，简洁大气 */
.login-page .btn-login.el-button {
  width: 100% !important;
  min-height: 46px !important;
  height: 46px !important;
  padding: 0 20px !important;
  font-size: 1rem !important;
  font-weight: 600 !important;
  border-radius: 10px !important;
  color: #ffffff !important;
  text-shadow: none !important;
  background: linear-gradient(90deg, #00d4ff 0%, #6366f1 50%, #a855f7 100%) !important;
  border: none !important;
  box-shadow: 0 4px 24px rgba(0,212,255,0.4), 0 4px 20px rgba(124,58,237,0.25) !important;
  transition: transform 0.2s ease, box-shadow 0.25s ease;
}
.login-page .btn-login.el-button:hover {
  color: #ffffff !important;
  box-shadow: 0 6px 32px rgba(0,212,255,0.5), 0 6px 28px rgba(124,58,237,0.35) !important;
  transform: translateY(-2px);
}
.login-page .btn-login.el-button:active {
  transform: translateY(0);
  box-shadow: 0 4px 24px rgba(0,212,255,0.35) !important;
}
</style>
