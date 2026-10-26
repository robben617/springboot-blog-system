import request from '@/utils/request'

// 文章相关 API
export const articleApi = {
  // 获取文章列表
  getArticleList(params) {
    return request({
      url: '/articles',
      method: 'get',
      params
    })
  },

  // 获取文章详情
  getArticleDetail(id) {
    return request({
      url: `/articles/${id}`,
      method: 'get'
    })
  },

  // 编辑回显（仅作者/管理员，不增加浏览量）
  getArticleForEdit(id) {
    return request({
      url: `/article/${id}/edit`,
      method: 'get'
    })
  },

  // 获取我的文章列表
  getMyArticles(params) {
    return request({
      url: '/article/my',
      method: 'get',
      params
    })
  },

  // 创建文章
  createArticle(data) {
    return request({
      url: '/article',
      method: 'post',
      data
    })
  },

  // 更新文章
  updateArticle(id, data) {
    return request({
      url: `/article/${id}`,
      method: 'put',
      data
    })
  },

  // 删除文章
  deleteArticle(id) {
    return request({
      url: `/article/${id}`,
      method: 'delete'
    })
  },

  // 发布文章
  publishArticle(id) {
    return request({
      url: `/article/${id}/publish`,
      method: 'put'
    })
  },

  // 转为草稿
  draftArticle(id) {
    return request({
      url: `/article/${id}/draft`,
      method: 'put'
    })
  },

  // 上传文章封面
  uploadCover(formData) {
    return request({
      url: '/article/cover',
      method: 'post',
      data: formData,
      headers: {
        'Content-Type': 'multipart/form-data'
      }
    })
  }
}

export default articleApi
