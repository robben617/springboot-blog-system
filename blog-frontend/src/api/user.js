import request from '@/utils/request'

// 用户相关 API
export const userApi = {
  // 注册
  register(data) {
    return request({
      url: '/register',
      method: 'post',
      data
    })
  },

  // 登录
  login(data) {
    return request({
      url: '/login',
      method: 'post',
      data
    })
  },

  // 退出登录
  logout() {
    return request({
      url: '/logout',
      method: 'post'
    })
  },

  // 获取当前登录用户信息
  getUserInfo() {
    return request({
      url: '/user/info',
      method: 'get'
    })
  },

  // 获取个人创作统计
  getStats() {
    return request({
      url: '/user/stats',
      method: 'get'
    })
  },

  // 修改个人资料
  updateProfile(data) {
    return request({
      url: '/user/profile',
      method: 'put',
      data
    })
  },

  // 修改密码
  changePassword(data) {
    return request({
      url: '/user/password',
      method: 'put',
      data
    })
  },

  // 上传头像
  uploadAvatar(formData) {
    return request({
      url: '/user/avatar',
      method: 'post',
      data: formData,
      headers: {
        'Content-Type': 'multipart/form-data'
      }
    })
  },

  // 获取指定用户信息
  getUserById(id) {
    return request({
      url: `/user/${id}`,
      method: 'get'
    })
  }
}

export default userApi
