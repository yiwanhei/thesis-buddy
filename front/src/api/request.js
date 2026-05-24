import axios from 'axios'

// 创建 axios 实例，用于统一配置后端接口请求
const request = axios.create({
    baseURL: '/api', // 基础 URL，所有请求将自动拼接此前缀
    timeout: 10000,   // 请求超时时间，单位为毫秒
})

// 请求拦截器
request.interceptors.request.use(
    config => {
        // 可以在这里添加 token 等认证信息
        return config
    },
    error => {
        console.error('请求错误:', error)
        return Promise.reject(error)
    }
)

// 响应拦截器
request.interceptors.response.use(
    response => {
        return response
    },
    error => {
        console.error('响应错误:', error)
        return Promise.reject(error)
    }
)

export { request }
