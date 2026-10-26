import { defineStore } from 'pinia'
import { userApi } from '@/api/user'

const STORAGE_KEY = 'blog_user_info'

export const useUserStore = defineStore('user', {
  state: () => ({
    userInfo: {
      id: null,
      username: '',
      nickname: '',
      email: '',
      avatar: '',
      bio: '',
      role: 0, // 0-普通用户 1-管理员
      status: 1
    },
    isLoggedIn: false
  }),

  getters: {
    isAdmin: (state) => state.userInfo.role === 1,
    userId: (state) => state.userInfo.id
  },

  actions: {
    // 登录
    async login(loginForm) {
      const data = await userApi.login(loginForm)
      this.setUserInfo(data)
      this.saveToStorage()
      return data
    },

    // 注册
    async register(registerForm) {
      const data = await userApi.register(registerForm)
      return data
    },

    // 退出登录
    async logout() {
      try {
        await userApi.logout()
      } catch (e) {
        // 忽略退出接口错误，直接清除本地状态
        console.warn('Logout API error:', e)
      }
      this.clearUserInfo()
      this.removeFromStorage()
    },

    // 获取用户信息
    async fetchUserInfo() {
      try {
        const data = await userApi.getUserInfo()
        this.setUserInfo(data)
        this.saveToStorage()
        return data
      } catch (e) {
        this.clearUserInfo()
        this.removeFromStorage()
        throw e
      }
    },

    // 更新用户信息
    async updateProfile(data) {
      const result = await userApi.updateProfile(data)
      // 更新本地存储的信息
      this.userInfo = { ...this.userInfo, ...data }
      this.saveToStorage()
      return result
    },

    // 修改密码
    async changePassword(data) {
      return await userApi.changePassword(data)
    },

    // 上传头像
    async uploadAvatar(formData) {
      const result = await userApi.uploadAvatar(formData)
      this.userInfo.avatar = result
      this.saveToStorage()
      return result
    },

    // 设置用户信息
    setUserInfo(data) {
      this.userInfo = {
        id: data.id,
        username: data.username,
        nickname: data.nickname,
        email: data.email,
        avatar: data.avatar,
        bio: data.bio,
        role: data.role,
        status: data.status
      }
      this.isLoggedIn = true
    },

    // 清除用户信息
    clearUserInfo() {
      this.userInfo = {
        id: null,
        username: '',
        nickname: '',
        email: '',
        avatar: '',
        bio: '',
        role: 0,
        status: 1
      }
      this.isLoggedIn = false
    },

    // 保存到 localStorage
    saveToStorage() {
      localStorage.setItem(STORAGE_KEY, JSON.stringify({
        userInfo: this.userInfo,
        isLoggedIn: this.isLoggedIn
      }))
    },

    // 从 localStorage 恢复
    restoreFromStorage() {
      const stored = localStorage.getItem(STORAGE_KEY)
      if (stored) {
        try {
          const data = JSON.parse(stored)
          if (data.isLoggedIn && data.userInfo) {
            this.userInfo = data.userInfo
            this.isLoggedIn = data.isLoggedIn
          }
        } catch (e) {
          console.error('Failed to restore user info from storage:', e)
          this.removeFromStorage()
        }
      }
    },

    // 从 localStorage 移除
    removeFromStorage() {
      localStorage.removeItem(STORAGE_KEY)
    }
  }
})
