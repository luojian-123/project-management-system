import { defineStore } from 'pinia'
import { login as apiLogin, getInfo } from '@/api/auth'
import { menuTree } from '@/api/menu'

async function setMenusForUser(store, data) {
  if (data.username === 'admin') {
    try {
      const fullTree = await menuTree(true)
      store.menus = Array.isArray(fullTree) ? fullTree : (fullTree?.data ?? fullTree?.children ?? [])
    } catch {
      store.menus = data.menus || []
    }
  } else {
    store.menus = data.menus || []
  }
}

function getStoredUser() {
  try {
    const raw = localStorage.getItem('user')
    if (!raw) return {}
    return JSON.parse(raw)
  } catch {
    return {}
  }
}

export const useUserStore = defineStore('user', {
  state: () => {
    const stored = getStoredUser()
    return {
      token: localStorage.getItem('token') || '',
      userId: stored.userId ?? null,
      username: stored.username ?? '',
      realName: stored.realName ?? '',
      avatar: stored.avatar ?? null,
      menus: [],
      roleCodes: stored.roleCodes ?? []
    }
  },
  actions: {
    async login(credentials) {
      const data = await apiLogin(credentials)
      this.token = data.token
      this.userId = data.userId
      this.username = data.username
      this.realName = data.realName
      this.roleCodes = Array.isArray(data.roleCodes) ? data.roleCodes : []
      await setMenusForUser(this, data)
      localStorage.setItem('token', data.token)
      localStorage.setItem('user', JSON.stringify({ userId: data.userId, username: data.username, realName: data.realName, avatar: data.avatar ?? null, roleCodes: this.roleCodes }))
    },
    async fetchInfo() {
      const data = await getInfo()
      this.userId = data.userId
      this.username = data.username
      this.realName = data.realName
      // 接口有头像则用接口的；否则保留本地已保存的头像（避免刷新后丢失）
      if (data.avatar != null && data.avatar !== '') {
        this.avatar = data.avatar
      } else {
        const stored = getStoredUser()
        if (stored.avatar) this.avatar = stored.avatar
      }
      this.roleCodes = Array.isArray(data.roleCodes) ? data.roleCodes : []
      await setMenusForUser(this, data)
      localStorage.setItem('user', JSON.stringify({ userId: this.userId, username: this.username, realName: this.realName, avatar: this.avatar ?? null, roleCodes: this.roleCodes }))
    },
    logout() {
      this.token = ''
      this.userId = null
      this.username = ''
      this.realName = ''
      this.avatar = null
      this.menus = []
      this.roleCodes = []
      localStorage.removeItem('token')
      localStorage.removeItem('user')
    },
    /** 设置头像（本地图片 URL 或 base64），并持久化到 localStorage */
    setAvatar(url) {
      this.avatar = url
      const raw = localStorage.getItem('user')
      let obj = {}
      try {
        if (raw) obj = JSON.parse(raw)
      } catch (_) {}
      obj.avatar = url
      localStorage.setItem('user', JSON.stringify(obj))
    }
  }
})
