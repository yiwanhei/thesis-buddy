/* jshint esversion: 11, asi: true, unused: false, strict: false, module: true */
import { createRouter, createWebHistory } from 'vue-router'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/login',
      name: 'Login',
      component: () => import('../views/student/LoginView.vue'),
    },
    {
      path: '/',
      name: 'StudentLayout',
      component: () => import('../views/student/StudentLayout.vue'),
      redirect: '/topics', // 修改：默认跳转到论文选题页面
      children: [
        {
          path: 'topics',
          name: 'TopicList',
          component: () => import('../views/student/TopicListView.vue'),
        },
        {
          path: 'team',
          name: 'Team',
          component: () => import('../views/student/TeamView.vue'),
        },
        {
          path: 'application',
          name: 'Application',
          component: () => import('../views/student/ApplicationView.vue'),
        },
      ],
    },
  ],
})

// 路由守卫：检查登录状态和过期时间
router.beforeEach((to, _from) => {
  // 如果访问登录页，直接放行
  if (to.path === '/login') {
    return true
  }
  
  // 检查是否有 studentId 和未过期的登录状态
  const studentId = localStorage.getItem('studentId')
  const expireTime = localStorage.getItem('loginExpireTime')
  
  if (!studentId || !expireTime) {
    // 没有登录信息，跳转到登录页
    return '/login'
  }
  
  // 检查是否过期
  const now = Date.now()
  if (now > parseInt(expireTime)) {
    // 已过期，清除所有登录信息并跳转到登录页
    localStorage.removeItem('studentId')
    localStorage.removeItem('studentInfo')
    localStorage.removeItem('loginExpireTime')
    return '/login'
  }
  
  // 未过期，允许访问
  return true
})

export default router
