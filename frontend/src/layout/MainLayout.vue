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
        <div class="header-right">
          <el-button type="primary" link class="header-ai-btn" @click="onAiAssistantClick">
            <el-icon><MagicStick /></el-icon>
            <span>AI开发助手</span>
          </el-button>
          <el-dropdown trigger="click" @command="handleUserCommand" class="user-dropdown">
          <div class="user-wrap">
            <el-avatar :key="(userStore.userId ?? '') + (userStore.avatar ?? '')" :size="28" :src="avatarSrc" class="header-avatar">
              {{ avatarText }}
            </el-avatar>
            <span class="user-name">{{ displayName }}</span>
            <el-icon class="user-arrow"><ArrowDown /></el-icon>
          </div>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item command="password">修改密码</el-dropdown-item>
              <el-dropdown-item command="logout" divided>退出</el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
        </div>
      </el-header>
      <el-main class="main">
        <ErrorBoundary>
          <router-view :key="route.fullPath" />
        </ErrorBoundary>
      </el-main>
    </el-container>
  </el-container>

  <!-- AI开发助手 聊天框 -->
  <el-dialog
    v-model="aiChatVisible"
    title="AI开发助手"
    width="480px"
    class="ai-chat-dialog"
    :close-on-click-modal="true"
    destroy-on-close
    @closed="aiChatInput = ''"
  >
    <div class="ai-chat-body">
      <div class="ai-chat-messages">
        <div class="ai-chat-msg ai-chat-msg--assistant">
          <div class="ai-chat-msg-avatar">杰</div>
          <div class="ai-chat-msg-content">
            <div class="ai-chat-msg-name">小杰</div>
            <div class="ai-chat-msg-text">{{ aiGreeting }}</div>
          </div>
        </div>
      </div>
      <div class="ai-chat-footer">
        <el-input
          v-model="aiChatInput"
          type="textarea"
          :rows="2"
          placeholder="输入您的要求，如：生成登录接口、部署说明..."
          maxlength="500"
          show-word-limit
          class="ai-chat-input"
          @keydown.enter.ctrl="sendAiMessage"
        />
        <el-button type="primary" class="ai-chat-send" :disabled="!aiChatInput.trim()" @click="sendAiMessage">发送</el-button>
      </div>
    </div>
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
import { List, Folder, Money, Warning, Edit, Flag, Setting, User, OfficeBuilding, ArrowDown, MagicStick } from '@element-plus/icons-vue'
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
const avatarSrc = computed(() => {
  const v = userStore.avatar
  return (v && typeof v === 'string' && v.trim()) ? v : undefined
})
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

const aiChatVisible = ref(false)
const aiChatInput = ref('')
const aiGreeting = '您好，我是AI开发工程师小杰，您可以提出您的要求，我可以根据要求生成前后端代码，及部署，完全符合开发工程学，'

function onAiAssistantClick() {
  aiChatVisible.value = true
}

function sendAiMessage() {
  const text = aiChatInput.value?.trim()
  if (!text) return
  ElMessage.info('对话能力即将开放，敬请期待')
  aiChatInput.value = ''
}

function handleUserCommand(command) {
  if (command === 'logout') {
    logout()
    return
  }
  if (command === 'password') {
    passwordDialogVisible.value = true
    return
  }
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
  font-size: var(--text-xl);
  font-weight: var(--font-weight-bold);
  letter-spacing: 0.04em;
  line-height: var(--leading-tight);
  background: linear-gradient(135deg, #00d4ff 0%, #7c3aed 50%, #a855f7 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  filter: drop-shadow(0 0 12px rgba(0,212,255,0.4));
}
.aside-menu {
  border-right: none !important;
  padding: 12px 0;
}
.aside-menu .el-menu-item {
  margin: 2px 10px;
  border-radius: var(--radius-md);
  height: 44px;
  line-height: 44px;
  font-size: var(--text-sm);
  font-weight: var(--font-weight-medium);
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
  font-weight: var(--font-weight-semibold);
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
  font-size: var(--text-lg);
  font-weight: var(--font-weight-semibold);
  line-height: var(--leading-tight);
  letter-spacing: var(--tracking-normal);
  background: linear-gradient(135deg, #0f172a 0%, #334155 50%, #1e293b 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}
.header-right {
  display: flex;
  align-items: center;
  gap: 16px;
}
.header-ai-btn {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  font-size: var(--text-sm);
  font-weight: var(--font-weight-medium);
  color: var(--primary-start);
}
.header-ai-btn:hover { color: var(--tech-purple); }
.user-dropdown { cursor: pointer; }
.user-wrap {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 4px 10px;
  border-radius: var(--radius-md);
  transition: background 0.2s ease;
}
.user-wrap:hover { background: rgba(0, 212, 255, 0.06); }
.header-avatar {
  flex-shrink: 0;
  background: linear-gradient(135deg, var(--tech-cyan), var(--tech-purple));
  color: #fff;
  font-weight: var(--font-weight-semibold);
  font-size: var(--text-md);
}
.user-name { font-size: var(--text-md); font-weight: var(--font-weight-medium); color: #334155; }
.user-arrow { font-size: var(--text-xs); color: #94a3b8; margin-left: 2px; }
.main {
  flex: 1;
  background: var(--bg-main);
  padding: var(--space-page);
  overflow: auto;
  font-size: var(--text-base);
  line-height: var(--leading-normal);
}
/* AI 聊天框 */
.ai-chat-dialog .el-dialog__body { padding: 0; max-height: 70vh; }
.ai-chat-body { display: flex; flex-direction: column; min-height: 320px; }
.ai-chat-messages {
  flex: 1;
  overflow-y: auto;
  padding: 16px;
  min-height: 200px;
}
.ai-chat-msg {
  display: flex;
  gap: 12px;
  margin-bottom: 16px;
}
.ai-chat-msg--assistant .ai-chat-msg-content { background: linear-gradient(135deg, rgba(14,165,233,0.08), rgba(124,58,237,0.06)); border-radius: 12px; padding: 12px 14px; }
.ai-chat-msg-avatar {
  flex-shrink: 0;
  width: 36px;
  height: 36px;
  border-radius: 10px;
  background: linear-gradient(135deg, var(--tech-cyan), var(--tech-purple));
  color: #fff;
  font-size: 0.875rem;
  font-weight: 600;
  display: flex;
  align-items: center;
  justify-content: center;
}
.ai-chat-msg-name { font-size: 0.75rem; font-weight: 600; color: var(--tech-purple); margin-bottom: 4px; }
.ai-chat-msg-text { font-size: 0.875rem; line-height: 1.6; color: var(--text-primary); white-space: pre-wrap; word-break: break-word; }
.ai-chat-footer {
  padding: 12px 16px;
  border-top: 1px solid var(--border-light);
  background: var(--bg-header);
  display: flex;
  gap: 10px;
  align-items: flex-end;
}
.ai-chat-input { flex: 1; }
.ai-chat-send { flex-shrink: 0; }
</style>
