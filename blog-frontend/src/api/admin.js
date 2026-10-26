import request from '@/utils/request'

// 后台管理 API（所有 /admin/** 接口由后端 AdminInterceptor 校验管理员身份）
export const adminApi = {
  // 概览统计
  getStats() {
    return request({ url: '/admin/stats', method: 'get' })
  },

  // ============ 用户管理 ============
  getUsers(params) {
    return request({ url: '/admin/users', method: 'get', params })
  },
  updateUserStatus(id, status) {
    return request({ url: `/admin/user/${id}/status`, method: 'put', data: { status } })
  },
  deleteUser(id) {
    return request({ url: `/admin/user/${id}`, method: 'delete' })
  },
  resetUserPassword(id) {
    return request({ url: `/admin/user/${id}/reset-password`, method: 'put' })
  },

  // ============ 文章管理 ============
  getArticles(params) {
    return request({ url: '/admin/articles', method: 'get', params })
  },
  // 删除文章复用作者侧接口，后端已放行管理员
  deleteArticle(id) {
    return request({ url: `/article/${id}`, method: 'delete' })
  },

  // ============ 评论管理 ============
  getComments(params) {
    return request({ url: '/admin/comments', method: 'get', params })
  },
  // 删除评论复用作者侧接口，后端已放行管理员
  deleteComment(id) {
    return request({ url: `/comment/${id}`, method: 'delete' })
  },

  // ============ 分类管理 ============
  createCategory(data) {
    return request({ url: '/admin/category', method: 'post', data })
  },
  updateCategory(id, data) {
    return request({ url: `/admin/category/${id}`, method: 'put', data })
  },
  deleteCategory(id) {
    return request({ url: `/admin/category/${id}`, method: 'delete' })
  },

  // ============ 标签管理 ============
  createTag(data) {
    return request({ url: '/admin/tag', method: 'post', data })
  },
  updateTag(id, data) {
    return request({ url: `/admin/tag/${id}`, method: 'put', data })
  },
  deleteTag(id) {
    return request({ url: `/admin/tag/${id}`, method: 'delete' })
  },

  // ============ 公告管理 ============
  createAnnouncement(data) {
    return request({ url: '/admin/announcement', method: 'post', data })
  },
  updateAnnouncement(id, data) {
    return request({ url: `/admin/announcement/${id}`, method: 'put', data })
  },
  deleteAnnouncement(id) {
    return request({ url: `/admin/announcement/${id}`, method: 'delete' })
  },

  // ============ 友链管理 ============
  createFriendLink(data) {
    return request({ url: '/admin/friend-link', method: 'post', data })
  },
  updateFriendLink(id, data) {
    return request({ url: `/admin/friend-link/${id}`, method: 'put', data })
  },
  deleteFriendLink(id) {
    return request({ url: `/admin/friend-link/${id}`, method: 'delete' })
  }
}

export default adminApi
