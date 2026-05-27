import axios from 'axios'

// 创建 axios 实例，用于统一配置后端接口请求
const request = axios.create({
    baseURL: '/api',
    timeout: 10000,
})

// 请求拦截器
request.interceptors.request.use(
    config => {
        // 管理员请求自动添加JWT Token
        if (config.url && config.url.startsWith('/admin/')) {
            const token = localStorage.getItem('adminToken')
            if (token) {
                config.headers.Authorization = 'Bearer ' + token
            }
        }
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
        // 如果是管理员请求且返回401，自动跳转到登录页
        if (response.config.url && response.config.url.startsWith('/admin/')) {
            if (response.data && response.data.code === 401) {
                localStorage.removeItem('adminToken')
                localStorage.removeItem('adminInfo')
                window.location.href = '/admin/login'
            }
        }
        return response
    },
    error => {
        console.error('响应错误:', error)
        return Promise.reject(error)
    }
)

export { request }
