import { defineStore } from 'pinia'
import { login as apiLogin, getInfo } from '@/api/auth'

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
      menus: []
    }
  },
  actions: {
    async login(credentials) {
      const data = await apiLogin(credentials)
      this.token = data.token
      this.userId = data.userId
      this.username = data.username
      this.realName = data.realName
      this.menus = data.menus || []
      localStorage.setItem('token', data.token)
      localStorage.setItem('user', JSON.stringify({ userId: data.userId, username: data.username, realName: data.realName, avatar: data.avatar ?? null }))
    },
    async fetchInfo() {
      const data = await getInfo()
      this.userId = data.userId
      this.username = data.username
      this.realName = data.realName
      if (data.avatar != null) this.avatar = data.avatar
      this.menus = data.menus || []
    },
    logout() {
      this.token = ''
      this.userId = null
      this.username = ''
      this.realName = ''
      this.avatar = null
      this.menus = []
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
