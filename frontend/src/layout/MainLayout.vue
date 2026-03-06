<template>
  <el-container class="main-layout">
    <el-aside width="240px" class="aside">
      <div class="logo-wrap">
        <span class="logo-text">项目管理系统</span>
      </div>
      <el-menu
        :default-active="$route.path"
        router
        class="aside-menu"
        background-color="transparent"
        text-color="rgba(226,232,240,0.85)"
        active-text-color="#00d4ff"
      >
        <template v-if="menuItems.length">
          <el-menu-item v-for="m in menuItems" :key="m.path" :index="m.path">
            <el-icon><component :is="iconMap[m.icon] || Setting" /></el-icon>
            <span>{{ m.name }}</span>
          </el-menu-item>
        </template>
        <template v-else>
          <el-menu-item index="/todo"><el-icon><List /></el-icon><span>个人待办</span></el-menu-item>
          <el-menu-item index="/project"><el-icon><Folder /></el-icon><span>项目管理</span></el-menu-item>
          <el-menu-item index="/cost"><el-icon><Money /></el-icon><span>成本管理</span></el-menu-item>
          <el-menu-item index="/issue"><el-icon><Warning /></el-icon><span>问题管理</span></el-menu-item>
          <el-menu-item index="/change"><el-icon><Edit /></el-icon><span>变更管理</span></el-menu-item>
          <el-menu-item index="/risk"><el-icon><Flag /></el-icon><span>风险管理</span></el-menu-item>
          <el-menu-item index="/org"><el-icon><OfficeBuilding /></el-icon><span>组织管理</span></el-menu-item>
        </template>
      </el-menu>
    </el-aside>
    <el-container class="right-wrap">
      <el-header class="header">
        <span class="title">{{ pageTitle }}</span>
        <el-dropdown trigger="click" @command="handleUserCommand" class="user-dropdown">
          <div class="user-wrap">
            <el-avatar :size="36" :src="userStore.avatar" class="header-avatar">
              {{ avatarText }}
            </el-avatar>
            <span class="user-name">{{ displayName }}</span>
            <el-icon class="user-arrow"><ArrowDown /></el-icon>
          </div>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item command="avatar">修改头像</el-dropdown-item>
              <el-dropdown-item command="password">修改密码</el-dropdown-item>
              <el-dropdown-item command="logout" divided>退出</el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </el-header>
      <el-main class="main">
        <ErrorBoundary>
          <router-view :key="route.fullPath" />
        </ErrorBoundary>
      </el-main>
    </el-container>
  </el-container>

  <!-- 修改头像 -->
  <el-dialog v-model="avatarDialogVisible" title="修改头像" width="400px" @close="onAvatarDialogClose">
    <div class="avatar-upload-wrap">
      <input ref="avatarInputRef" type="file" accept="image/*" class="avatar-input" @change="onAvatarFileChange" />
      <el-button type="primary" @click="avatarInputRef?.click()">选择本地图片</el-button>
      <div v-if="avatarPreview" class="avatar-preview">
        <el-avatar :size="80" :src="avatarPreview" />
      </div>
    </div>
    <template #footer>
      <el-button @click="avatarDialogVisible = false">取消</el-button>
      <el-button type="primary" :disabled="!avatarPreview" @click="confirmAvatar">确定</el-button>
    </template>
  </el-dialog>

  <!-- 修改密码 -->
  <el-dialog v-model="passwordDialogVisible" title="修改密码" width="400px" @close="passwordFormRef?.resetFields()">
    <el-form ref="passwordFormRef" :model="passwordForm" :rules="passwordRules" label-width="90px">
      <el-form-item label="原密码" prop="oldPassword">
        <el-input v-model="passwordForm.oldPassword" type="password" placeholder="请输入原密码" show-password />
      </el-form-item>
      <el-form-item label="新密码" prop="newPassword">
        <el-input v-model="passwordForm.newPassword" type="password" placeholder="至少6位" show-password />
      </el-form-item>
      <el-form-item label="确认新密码" prop="confirmPassword">
        <el-input v-model="passwordForm.confirmPassword" type="password" placeholder="再次输入新密码" show-password />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="passwordDialogVisible = false">取消</el-button>
      <el-button type="primary" :loading="passwordSubmitting" @click="confirmPassword">确定</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { computed, onMounted, ref, reactive } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/store/user'
import { List, Folder, Money, Warning, Edit, Flag, Setting, User, OfficeBuilding, ArrowDown } from '@element-plus/icons-vue'
import { changePassword } from '@/api/auth'
import ErrorBoundary from '@/components/ErrorBoundary.vue'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

onMounted(() => {
  if (userStore.token && !userStore.username) userStore.fetchInfo()
})

const iconMap = { List, Folder, Money, Warning, Edit, Flag, Setting, OfficeBuilding }

const defaultMenus = [
  { path: '/todo', name: '个人待办', icon: 'List' },
  { path: '/project', name: '项目管理', icon: 'Folder' },
  { path: '/cost', name: '成本管理', icon: 'Money' },
  { path: '/issue', name: '问题管理', icon: 'Warning' },
  { path: '/change', name: '变更管理', icon: 'Edit' },
  { path: '/risk', name: '风险管理', icon: 'Flag' },
  { path: '/org', name: '组织管理', icon: 'OfficeBuilding' }
]

const menuItems = computed(() => {
  const menus = userStore.menus || []
  function flatten(items) {
    const out = []
    for (const m of items) {
      if (m.path) out.push({ path: m.path, name: m.name, icon: m.icon })
      if (m.children?.length) out.push(...flatten(m.children))
    }
    return out
  }
  const fromBackend = flatten(menus)
  if (fromBackend.length === 0) return defaultMenus
  const paths = new Set(fromBackend.map(m => m.path))
  const merged = [...fromBackend]
  for (const d of defaultMenus) {
    if (!paths.has(d.path)) {
      merged.push(d)
      paths.add(d.path)
    }
  }
  merged.sort((a, b) => defaultMenus.findIndex(m => m.path === a.path) - defaultMenus.findIndex(m => m.path === b.path))
  return merged
})

const displayName = computed(() => userStore.realName || userStore.username || '未登录')
/** 头像无图时显示的首字（中文取首字，英文取首字母） */
const avatarText = computed(() => {
  const name = (userStore.realName || userStore.username || '').trim()
  if (!name) return '管'
  if (/[\u4e00-\u9fa5]/.test(name)) return name.slice(0, 1)
  return name.slice(0, 1).toUpperCase()
})

const pageTitle = computed(() => {
  const path = route.path
  const byPath = {}
  function collect(items) {
    for (const m of items || []) {
      if (m.path) byPath[m.path] = m.name
      if (m.children?.length) collect(m.children)
    }
  }
  collect(userStore.menus || [])
  if (byPath[path]) return byPath[path]
  if (path.startsWith('/project/')) return '项目详情'
  if (path.startsWith('/task/')) return '任务详情'
  if (path.startsWith('/model/')) return byPath['/model/' + route.params.formCode] || '对象管理'
  const fallback = { '/todo': '个人待办', '/project': '项目管理', '/cost': '成本管理', '/issue': '问题管理', '/change': '变更管理', '/risk': '风险管理', '/org': '组织管理' }
  return fallback[path] || '项目管理系统'
})

function handleUserCommand(command) {
  if (command === 'logout') {
    logout()
    return
  }
  if (command === 'avatar') {
    avatarPreview.value = ''
    avatarFile = null
    if (avatarInputRef.value) avatarInputRef.value.value = ''
    avatarDialogVisible.value = true
    return
  }
  if (command === 'password') {
    passwordDialogVisible.value = true
    return
  }
}

// 修改头像
const avatarDialogVisible = ref(false)
const avatarInputRef = ref(null)
const avatarPreview = ref('')
let avatarFile = null

function onAvatarDialogClose() {
  avatarPreview.value = ''
  avatarFile = null
}

function onAvatarFileChange(e) {
  const file = e.target.files?.[0]
  if (!file || !file.type.startsWith('image/')) {
    ElMessage.warning('请选择图片文件')
    return
  }
  avatarFile = file
  const reader = new FileReader()
  reader.onload = () => { avatarPreview.value = reader.result }
  reader.readAsDataURL(file)
}

function confirmAvatar() {
  if (!avatarPreview.value) return
  userStore.setAvatar(avatarPreview.value)
  ElMessage.success('头像已更新')
  avatarDialogVisible.value = false
}

// 修改密码
const passwordDialogVisible = ref(false)
const passwordFormRef = ref(null)
const passwordSubmitting = ref(false)
const passwordForm = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})
const validateConfirm = (rule, value, callback) => {
  if (value !== passwordForm.newPassword) callback(new Error('两次输入的新密码不一致'))
  else callback()
}
const passwordRules = {
  oldPassword: [{ required: true, message: '请输入原密码', trigger: 'blur' }],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, message: '新密码至少6位', trigger: 'blur' }
  ],
  confirmPassword: [{ required: true, validator: validateConfirm, trigger: 'blur' }]
}

async function confirmPassword() {
  await passwordFormRef.value?.validate().catch(() => {})
  passwordSubmitting.value = true
  try {
    await changePassword({
      oldPassword: passwordForm.oldPassword,
      newPassword: passwordForm.newPassword
    })
    ElMessage.success('密码已修改，请使用新密码登录')
    passwordDialogVisible.value = false
    logout()
  } catch (e) {
    ElMessage.error(e?.message || '修改失败')
  } finally {
    passwordSubmitting.value = false
  }
}

function logout() {
  userStore.logout()
  router.push('/login')
}
</script>

<style scoped>
.main-layout { height: 100%; min-height: 100vh; }
.aside {
  background: linear-gradient(180deg, #0a0e27 0%, #131829 30%, #1a1f3a 70%, #0d1222 100%);
  box-shadow: 4px 0 32px rgba(0,212,255,0.06), inset -1px 0 0 rgba(0,212,255,0.08);
}
.logo-wrap {
  height: 64px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-bottom: 1px solid rgba(0,212,255,0.15);
  padding: 0 16px;
  position: relative;
}
.logo-wrap::after {
  content: '';
  position: absolute;
  bottom: -1px;
  left: 20%;
  right: 20%;
  height: 1px;
  background: linear-gradient(90deg, transparent, rgba(0,212,255,0.5), transparent);
  opacity: 0.8;
}
.logo-text {
  font-size: 1.35rem;
  font-weight: 700;
  background: linear-gradient(135deg, #00d4ff 0%, #7c3aed 50%, #a855f7 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  letter-spacing: 0.04em;
  filter: drop-shadow(0 0 12px rgba(0,212,255,0.4));
}
.aside-menu {
  border-right: none !important;
  padding: 12px 0;
}
.aside-menu .el-menu-item {
  margin: 2px 10px;
  border-radius: 8px;
  height: 44px;
  line-height: 44px;
  transition: all 0.25s ease;
}
.aside-menu .el-menu-item:hover {
  background: linear-gradient(90deg, rgba(0,212,255,0.15), rgba(124,58,237,0.1)) !important;
  color: #00d4ff !important;
  box-shadow: 0 0 20px rgba(0,212,255,0.15);
}
.aside-menu .el-menu-item.is-active {
  background: linear-gradient(90deg, rgba(0,212,255,0.2), rgba(124,58,237,0.15)) !important;
  color: #00d4ff !important;
  font-weight: 500;
  box-shadow: inset 0 0 20px rgba(0,212,255,0.08), 0 0 24px rgba(0,212,255,0.12);
  border: 1px solid rgba(0,212,255,0.25);
}
.right-wrap { flex: 1; display: flex; flex-direction: column; min-width: 0; }
.header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 56px;
  padding: 0 24px;
  background: linear-gradient(90deg, rgba(255,255,255,0.98) 0%, rgba(248,250,252,0.98) 100%);
  border-bottom: 1px solid rgba(0,212,255,0.12);
  box-shadow: 0 2px 16px rgba(0,0,0,0.04), 0 1px 0 rgba(0,212,255,0.06);
}
.title {
  font-size: 1.125rem;
  font-weight: 600;
  background: linear-gradient(135deg, #0f172a 0%, #334155 50%, #1e293b 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}
.user-dropdown { cursor: pointer; }
.user-wrap {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 4px 10px;
  border-radius: 8px;
  transition: background 0.2s ease;
}
.user-wrap:hover { background: rgba(0, 212, 255, 0.06); }
.header-avatar {
  flex-shrink: 0;
  background: linear-gradient(135deg, var(--tech-cyan), var(--tech-purple));
  color: #fff;
  font-weight: 600;
  font-size: 0.9375rem;
}
.user-name { font-size: 0.9375rem; font-weight: 500; color: #334155; }
.user-arrow { font-size: 0.75rem; color: #94a3b8; margin-left: 2px; }
.main {
  flex: 1;
  background: var(--bg-main);
  padding: 0.2cm;
  overflow: auto;
}
.avatar-input { display: none; }
.avatar-upload-wrap { display: flex; flex-direction: column; align-items: center; gap: 16px; }
.avatar-preview { margin-top: 8px; }
</style>
