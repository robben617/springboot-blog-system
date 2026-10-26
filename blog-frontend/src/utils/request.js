import axios from 'axios'
import { ElMessage } from 'element-plus'
import router from '@/router'
import { useUserStore } from '@/store/user'

// 创建 axios 实例
const request = axios.create({
  baseURL: '/api',
  timeout: 15000,
  withCredentials: true // 允许携带 cookie（Session 登录需要）
})

// 请求拦截器
request.interceptors.request.use(
  (config) => {
    // 可以在这里添加统一的请求头
    config.headers['Content-Type'] = config.headers['Content-Type'] || 'application/json'
    return config
  },
  (error) => {
    console.error('Request error:', error)
    return Promise.reject(error)
  }
)

// 响应拦截器
request.interceptors.response.use(
  (response) => {
    const res = response.data

    // 如果是文件下载，直接返回
    if (response.config.responseType === 'blob') {
      return response
    }

    // 业务成功：code === 200
    if (res.code === 200) {
      return res.data
    }

    // 业务错误
    // 401：未登录 / 登录过期
    if (res.code === 401) {
      const userStore = useUserStore()
      userStore.clearUserInfo()
      ElMessage.error(res.msg || '登录已过期，请重新登录')
      router.push('/login')
      return Promise.reject(new Error(res.msg || '未登录'))
    }

    // 403：无权限
    if (res.code === 403) {
      ElMessage.error(res.msg || '没有权限访问')
      return Promise.reject(new Error(res.msg || '无权限'))
    }

    // 404：资源不存在
    if (res.code === 404) {
      ElMessage.error(res.msg || '请求的资源不存在')
      return Promise.reject(new Error(res.msg || '不存在'))
    }

    // 其他业务错误
    ElMessage.error(res.msg || '操作失败')
    return Promise.reject(new Error(res.msg || '操作失败'))
  },
  (error) => {
    console.error('Response error:', error)

    // HTTP 状态码错误
    if (error.response) {
      const status = error.response.status

      if (status === 401) {
        const userStore = useUserStore()
        userStore.clearUserInfo()
        ElMessage.error('登录已过期，请重新登录')
        router.push('/login')
      } else if (status === 403) {
        ElMessage.error('没有权限访问')
      } else if (status === 404) {
        ElMessage.error('请求的接口不存在')
      } else if (status === 413) {
        ElMessage.error('上传的文件过大')
      } else if (status === 500) {
        ElMessage.error('服务器内部错误')
      } else {
        ElMessage.error(error.response.data?.msg || `请求失败 (${status})`)
      }
    } else if (error.request) {
      ElMessage.error('网络连接失败，请检查网络')
    } else {
      ElMessage.error(error.message || '请求出错')
    }

    return Promise.reject(error)
  }
)

export default request
