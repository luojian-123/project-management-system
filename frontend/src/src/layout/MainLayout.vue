<template>
  <el-container class="main-layout">
    <el-aside width="240px" class="aside">
      <div class="logo-wrap">
        <span class="logo-text">JWI Code</span>
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
          <el-menu-item index="/sys"><el-icon><Setting /></el-icon><span>系统配置</span></el-menu-item>
        </template>
      </el-menu>
    </el-aside>
    <el-container class="right-wrap">
      <el-header class="header">
        <span class="title">{{ pageTitle }}</span>
        <div class="user-wrap">
          <span class="user-name">{{ userStore.realName || userStore.username }}</span>
          <el-button type="primary" link class="btn-logout" @click="logout">退出</el-button>
        </div>
      </el-header>
      <el-main class="main">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/store/user'
import { List, Folder, Money, Warning, Edit, Flag, Setting } from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const iconMap = { List, Folder, Money, Warning, Edit, Flag, Setting }

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
  return flatten(menus)
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
  if (path.startsWith('/model/')) return byPath['/model/' + route.params.formCode] || '对象管理'
  const fallback = { '/todo': '个人待办', '/project': '项目管理', '/cost': '成本管理', '/issue': '问题管理', '/change': '变更管理', '/risk': '风险管理', '/sys': '系统配置' }
  return fallback[path] || 'JWI Code'
})

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
.user-wrap { display: flex; align-items: center; gap: 16px; }
.user-name { font-size: 0.875rem; color: #475569; }
.btn-logout { padding: 4px 8px; color: #7c3aed !important; }
.btn-logout:hover { color: #00d4ff !important; }
.main {
  flex: 1;
  background: linear-gradient(160deg, #f0f9ff 0%, #f8fafc 40%, #f1f5f9 100%);
  padding: 24px;
  overflow: auto;
}
</style>
