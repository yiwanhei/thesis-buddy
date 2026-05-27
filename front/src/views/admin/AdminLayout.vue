<template>
  <div class="admin-layout">
    <!-- 侧边栏 -->
    <aside class="sidebar" :class="{ collapsed: sidebarCollapsed }">
      <!-- Logo -->
      <div class="sidebar-header">
        <span class="logo-text" v-if="!sidebarCollapsed">📚 论文选题系统</span>
        <span class="logo-icon" v-else>📚</span>
      </div>

      <!-- 导航菜单 -->
      <nav class="sidebar-nav">
        <router-link
          v-for="item in menuItems"
          :key="item.path"
          :to="'/admin' + item.path"
          class="nav-item"
          active-class="active"
        >
          <span class="nav-icon">{{ item.icon }}</span>
          <span class="nav-label" v-if="!sidebarCollapsed">{{ item.label }}</span>
        </router-link>
      </nav>

      <!-- 底部 -->
      <div class="sidebar-footer">
        <div class="nav-item user-info" @click="logout">
          <span class="nav-icon">👤</span>
          <span class="nav-label" v-if="!sidebarCollapsed">
            {{ adminInfo?.account || 'admin' }}
            <span class="logout-hint">退出</span>
          </span>
        </div>
      </div>
    </aside>

    <!-- 主内容区域 -->
    <div class="main-area" :class="{ expanded: sidebarCollapsed }">
      <!-- 顶部栏 -->
      <header class="top-bar">
        <button class="collapse-btn" @click="sidebarCollapsed = !sidebarCollapsed">
          {{ sidebarCollapsed ? '☰' : '✕' }}
        </button>
        <span class="page-title">{{ currentPageTitle }}</span>
      </header>

      <!-- 内容区 -->
      <main class="content">
        <router-view />
      </main>
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'

const route = useRoute()
const router = useRouter()
const sidebarCollapsed = ref(false)

const adminInfo = computed(() => {
  try {
    return JSON.parse(localStorage.getItem('adminInfo') || '{}')
  } catch { return null }
})

const menuItems = [
  { path: '/dashboard', icon: '📊', label: '仪表盘' },
  { path: '/statistics', icon: '📈', label: '数据统计' },
  { path: '/users', icon: '👥', label: '用户管理' },
  { path: '/topics', icon: '📋', label: '选题管理' },
  { path: '/audit', icon: '✅', label: '审核管理' },
  { path: '/config', icon: '⚙️', label: '系统配置' },
  { path: '/teams', icon: '🚩', label: '队伍管理' },
]

const currentPageTitle = computed(() => {
  const item = menuItems.find(m => route.path.includes(m.path))
  return item ? item.label : '管理员后台'
})

function logout() {
  localStorage.removeItem('adminToken')
  localStorage.removeItem('adminInfo')
  void router.push('/admin/login')
}
</script>

<style scoped>
.admin-layout {
  display: flex;
  min-height: 100vh;
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
}

/* 侧边栏 */
.sidebar {
  width: 220px;
  background: #1a1a2e;
  color: #ccc;
  display: flex;
  flex-direction: column;
  transition: width 0.3s ease;
  flex-shrink: 0;
  overflow: hidden;
}

.sidebar.collapsed {
  width: 64px;
}

.sidebar-header {
  padding: 20px 16px;
  border-bottom: 1px solid rgba(255,255,255,0.06);
}

.logo-text {
  font-size: 15px;
  font-weight: 700;
  color: #fff;
  white-space: nowrap;
}

.logo-icon {
  font-size: 24px;
  display: flex;
  justify-content: center;
}

.sidebar-nav {
  flex: 1;
  padding: 12px 0;
}

.nav-item {
  display: flex;
  align-items: center;
  padding: 14px 20px;
  color: #aaa;
  text-decoration: none;
  font-size: 14px;
  transition: all 0.2s;
  cursor: pointer;
  white-space: nowrap;
}

.nav-item:hover {
  background: rgba(255,255,255,0.05);
  color: #fff;
}

.nav-item.active,
.nav-item.router-link-exact-active {
  background: #16213e;
  color: #e94560;
  border-left: 3px solid #e94560;
  padding-left: 17px;
}

.nav-icon {
  font-size: 18px;
  margin-right: 12px;
  flex-shrink: 0;
  width: 24px;
  text-align: center;
}

.sidebar.collapsed .nav-icon {
  margin-right: 0;
}

.nav-label {
  font-size: 14px;
}

/* 底部用户信息 */
.sidebar-footer {
  border-top: 1px solid rgba(255,255,255,0.06);
  padding: 8px 0;
}

.user-info .logout-hint {
  display: none;
  margin-left: 8px;
  font-size: 12px;
  color: #e94560;
}

.user-info:hover .logout-hint {
  display: inline;
}

/* 主内容区 */
.main-area {
  flex: 1;
  display: flex;
  flex-direction: column;
  background: #f5f7fa;
  min-width: 0;
}

.top-bar {
  display: flex;
  align-items: center;
  padding: 16px 24px;
  background: #fff;
  border-bottom: 1px solid #e8e8e8;
}

.collapse-btn {
  background: none;
  border: 1px solid #ddd;
  border-radius: 6px;
  padding: 6px 12px;
  font-size: 16px;
  cursor: pointer;
  color: #666;
  margin-right: 16px;
}

.collapse-btn:hover {
  background: #f0f0f0;
}

.page-title {
  font-size: 16px;
  font-weight: 600;
  color: #333;
}

.content {
  flex: 1;
  padding: 24px;
  overflow: auto;
}
</style>
