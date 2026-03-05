<template>
  <div class="login-page">
    <div class="login-bg-grid" />
    <div class="login-bg-glow" />
    <div class="login-bg-line" />
    <div class="login-box">
      <div class="login-header">
        <h1>JWI Code</h1>
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
  background: #060816;
  position: relative;
  overflow: hidden;
}
/* 科技网格底纹 */
.login-bg-grid {
  position: absolute;
  inset: 0;
  background-image:
    linear-gradient(rgba(0,212,255,0.03) 1px, transparent 1px),
    linear-gradient(90deg, rgba(0,212,255,0.03) 1px, transparent 1px);
  background-size: 48px 48px;
  pointer-events: none;
}
.login-bg-grid::after {
  content: '';
  position: absolute;
  inset: 0;
  background: linear-gradient(180deg, transparent 0%, rgba(6,8,22,0.4) 50%, #060816 100%);
  pointer-events: none;
}
/* 光晕 */
.login-bg-glow {
  position: absolute;
  width: 100%;
  height: 100%;
  top: 0;
  left: 0;
  background:
    radial-gradient(ellipse 80% 60% at 20% 30%, rgba(0,212,255,0.12) 0%, transparent 55%),
    radial-gradient(ellipse 70% 80% at 80% 70%, rgba(124,58,237,0.15) 0%, transparent 55%),
    radial-gradient(ellipse 50% 50% at 50% 50%, rgba(0,212,255,0.04) 0%, transparent 70%);
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
    rgba(0,212,255,0.02) 80px,
    rgba(0,212,255,0.02) 81px
  );
  pointer-events: none;
  animation: login-line-shift 25s linear infinite;
}
@keyframes login-line-shift {
  0% { transform: translate(0, 0); }
  100% { transform: translate(81px, 0); }
}
.login-box {
  width: 420px;
  padding: 48px 44px;
  background: linear-gradient(145deg, rgba(255,255,255,0.95) 0%, rgba(248,250,252,0.98) 100%);
  border-radius: 20px;
  box-shadow: 0 0 0 1px rgba(0,212,255,0.2),
              0 0 40px rgba(0,212,255,0.08),
              0 25px 50px -12px rgba(0,0,0,0.25),
              inset 0 1px 0 rgba(255,255,255,0.8);
  position: relative;
  backdrop-filter: blur(12px);
}
.login-box::before {
  content: '';
  position: absolute;
  inset: -1px;
  border-radius: 21px;
  padding: 1px;
  background: linear-gradient(135deg, rgba(0,212,255,0.5), rgba(124,58,237,0.4), rgba(168,85,247,0.3));
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
  background: linear-gradient(135deg, #00d4ff 0%, #7c3aed 50%, #a855f7 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  filter: drop-shadow(0 2px 8px rgba(0,212,255,0.3));
}
.login-desc { font-size: 0.875rem; color: #64748b; }
.form :deep(.el-form-item) { margin-bottom: 20px; }
.form :deep(.el-form-item:last-child) { margin-bottom: 0; margin-top: 12px; }
.form :deep(.input-tech .el-input__wrapper) {
  border-radius: 10px;
  box-shadow: 0 0 0 1px rgba(0,212,255,0.2);
  transition: box-shadow 0.25s ease;
}
.form :deep(.input-tech .el-input__wrapper:hover),
.form :deep(.input-tech .el-input__wrapper.is-focus) {
  box-shadow: 0 0 0 1px rgba(0,212,255,0.5), 0 0 16px rgba(0,212,255,0.1);
}
.btn-login {
  width: 100%;
  height: 46px;
  font-size: 1rem;
  font-weight: 600;
  border-radius: 10px;
  background: linear-gradient(135deg, #00d4ff 0%, #7c3aed 100%) !important;
  border: none !important;
  box-shadow: 0 4px 20px rgba(0,212,255,0.35), 0 0 0 0 rgba(124,58,237,0.2);
  transition: transform 0.2s ease, box-shadow 0.25s ease;
}
.btn-login:hover {
  box-shadow: 0 6px 28px rgba(0,212,255,0.45), 0 4px 20px rgba(124,58,237,0.3) !important;
  transform: translateY(-1px);
}
</style>
