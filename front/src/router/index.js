/* jshint esversion: 11, asi: true, unused: false, strict: false, module: true */
import { createRouter, createWebHistory } from 'vue-router'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    // ========== 学生端 ==========
    {
      path: '/login',
      name: 'Login',
      component: () => import('../views/student/LoginView.vue'),
    },
    {
      path: '/',
      name: 'StudentLayout',
      component: () => import('../views/student/StudentLayout.vue'),
      redirect: '/topics',
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

    // ========== 管理员端 ==========
    {
      path: '/admin/login',
      name: 'AdminLogin',
      component: () => import('../views/admin/AdminLoginView.vue'),
    },
    {
      path: '/admin',
      name: 'AdminLayout',
      component: () => import('../views/admin/AdminLayout.vue'),
      redirect: '/admin/dashboard',
      children: [
        {
          path: 'dashboard',
          name: 'AdminDashboard',
          component: () => import('../views/admin/DashboardView.vue'),
        },
        {
          path: 'statistics',
          name: 'AdminStatistics',
          component: () => import('../views/admin/StatisticsView.vue'),
        },
        {
          path: 'users',
          name: 'AdminUsers',
          component: () => import('../views/admin/UserManageView.vue'),
        },
        {
          path: 'topics',
          name: 'AdminTopics',
          component: () => import('../views/admin/TopicManageView.vue'),
        },
        {
          path: 'audit',
          name: 'AdminAudit',
          component: () => import('../views/admin/AuditManageView.vue'),
        },
        {
          path: 'config',
          name: 'AdminConfig',
          component: () => import('../views/admin/ConfigManageView.vue'),
        },
        {
          path: 'teams',
          name: 'AdminTeams',
          component: () => import('../views/admin/TeamManageView.vue'),
        },
      ],
    },
  ],
})

// 路由守卫
router.beforeEach((to, _from) => {
  // 管理员登录页 - 放行
  if (to.path === '/admin/login') {
    return true
  }

  // 管理员其他页面 - 检查adminToken
  if (to.path.startsWith('/admin')) {
    const token = localStorage.getItem('adminToken')
    if (!token) {
      return '/admin/login'
    }
    return true
  }

  // 学生登录页 - 放行
  if (to.path === '/login') {
    return true
  }

  // 学生端 - 检查studentId
  const studentId = localStorage.getItem('studentId')
  const expireTime = localStorage.getItem('loginExpireTime')

  if (!studentId || !expireTime) {
    return '/login'
  }

  if (Date.now() > parseInt(expireTime)) {
    localStorage.removeItem('studentId')
    localStorage.removeItem('studentInfo')
    localStorage.removeItem('loginExpireTime')
    return '/login'
  }

  return true
})

export default router
