import { defineStore } from 'pinia'
import { login as apiLogin, getInfo } from '@/api/auth'

export const useUserStore = defineStore('user', {
  state: () => ({
    token: localStorage.getItem('token') || '',
    userId: null,
    username: '',
    realName: '',
    menus: []
  }),
  actions: {
    async login(credentials) {
      const data = await apiLogin(credentials)
      this.token = data.token
      this.userId = data.userId
      this.username = data.username
      this.realName = data.realName
      this.menus = data.menus || []
      localStorage.setItem('token', data.token)
      localStorage.setItem('user', JSON.stringify({ userId: data.userId, username: data.username, realName: data.realName }))
    },
    async fetchInfo() {
      const data = await getInfo()
      this.userId = data.userId
      this.username = data.username
      this.realName = data.realName
      this.menus = data.menus || []
    },
    logout() {
      this.token = ''
      this.userId = null
      this.username = ''
      this.realName = ''
      this.menus = []
      localStorage.removeItem('token')
      localStorage.removeItem('user')
    }
  }
})
