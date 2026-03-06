import axios from 'axios'
import { ElMessage } from 'element-plus'
import router from '@/router'

const request = axios.create({
  baseURL: '/api',
  timeout: 15000
})

/** 解析后端校验错误：{ code: [{message:"..."}], name: [{message:"..."}] } -> 合并为一句提示 */
function parseValidationError(data) {
  if (!data || typeof data !== 'object') return null
  const list = []
  for (const key of Object.keys(data)) {
    const arr = data[key]
    if (Array.isArray(arr) && arr.length > 0 && arr[0]?.message) {
      list.push(arr[0].message)
    }
  }
  return list.length ? list.join('；') : null
}

request.interceptors.request.use(config => {
  const token = localStorage.getItem('token')
  if (token) config.headers.Authorization = `Bearer ${token}`
  return config
})

request.interceptors.response.use(
  res => {
    const { code, data, message } = res.data
    if (code === 200) return data
    const msg = parseValidationError(res.data) || message || '请求失败'
    ElMessage.error(msg)
    return Promise.reject(new Error(msg))
  },
  err => {
    if (err.response?.status === 401) {
      localStorage.removeItem('token')
      localStorage.removeItem('user')
      router.push('/login')
    }
    const body = err.response?.data
    const msg = parseValidationError(body) || body?.message || err.message || '网络错误'
    ElMessage.error(msg)
    return Promise.reject(err)
  }
)

export default request
