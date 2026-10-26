import request from '@/utils/request'

// 公共 API（分类、标签、评论、公告、友链等）
export const commonApi = {
  // 分类列表
  getCategories() {
    return request({
      url: '/categories',
      method: 'get'
    })
  },

  // 分类下的文章
  getCategoryArticles(categoryId, params) {
    return request({
      url: `/categories/${categoryId}/articles`,
      method: 'get',
      params
    })
  },

  // 标签列表
  getTags() {
    return request({
      url: '/tags',
      method: 'get'
    })
  },

  // 标签下的文章
  getTagArticles(tagId, params) {
    return request({
      url: `/tags/${tagId}/articles`,
      method: 'get',
      params
    })
  },

  // 评论列表
  getComments(articleId) {
    return request({
      url: `/comments/article/${articleId}`,
      method: 'get'
    })
  },

  // 发表评论
  createComment(data) {
    return request({
      url: '/comment',
      method: 'post',
      data
    })
  },

  // 删除评论
  deleteComment(id) {
    return request({
      url: `/comment/${id}`,
      method: 'delete'
    })
  },

  // 公告列表
  getAnnouncements() {
    return request({
      url: '/announcements',
      method: 'get'
    })
  },

  // 最新公告
  getLatestAnnouncements(limit = 3) {
    return request({
      url: '/announcements/latest',
      method: 'get',
      params: { limit }
    })
  },

  // 友链列表
  getFriendLinks() {
    return request({
      url: '/friend-links',
      method: 'get'
    })
  }
}

export default commonApi
