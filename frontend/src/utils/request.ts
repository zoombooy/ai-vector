import axios from 'axios'
import { message } from 'antd'

const request = axios.create({
  baseURL: '/api',
  timeout: 30000,
})

// 请求拦截器
request.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('token')
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  (error) => {
    return Promise.reject(error)
  }
)

// 响应拦截器
request.interceptors.response.use(
  (response) => {
    const { code, message: msg, data } = response.data
    
    if (code === 200) {
      return data
    } else if (code === 1001 || code === 1002 || code === 1003) {
      // Token相关错误，跳转登录
      localStorage.removeItem('token')
      window.location.href = '/login'
      message.error(msg || '认证失败，请重新登录')
      return Promise.reject(new Error(msg))
    } else {
      message.error(msg || '请求失败')
      return Promise.reject(new Error(msg))
    }
  },
  (error) => {
    if (error.response?.status === 401) {
      localStorage.removeItem('token')
      window.location.href = '/login'
      message.error('认证失败，请重新登录')
    } else {
      message.error(error.message || '网络错误')
    }
    return Promise.reject(error)
  }
)

export default request

