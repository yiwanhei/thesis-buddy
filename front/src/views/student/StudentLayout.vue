<template>
  <div class="layout">
    <!-- 左上角返回/退出按钮 -->
    <button @click="handleBackOrLogout" class="btn-back-logout">
      {{ showBackButton ? '返回' : '退出' }}
    </button>
    
    <!-- 主要内容区域 -->
    <main class="content">
      <router-view />
    </main>
    
    <!-- 底部导航按钮 -->
    <nav class="bottom-nav">
      <router-link to="/topics" class="nav-btn" active-class="active">
        论文选题
      </router-link>
      <router-link to="/team" class="nav-btn" active-class="active">
        队伍
      </router-link>
    </nav>
  </div>
</template>

<script setup>
import { ref, computed, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'

const router = useRouter()
const route = useRoute()

// 一级页面（顶层页面）列表
const topLevelPages = ['/topics', '/team']

// 判断是否显示返回按钮
const showBackButton = computed(() => {
  return !topLevelPages.includes(route.path)
})

// 处理返回或退出
const handleBackOrLogout = () => {
  if (showBackButton.value) {
    // 返回上一页
    router.back()
  } else {
    // 退出登录
    handleLogout()
  }
}

const handleLogout = () => {
  localStorage.removeItem('studentInfo')
  localStorage.removeItem('studentId')
  localStorage.removeItem('loginExpireTime')
  router.push('/login')
}
</script>

<style scoped>
.layout {
  min-height: 100vh;
  background: #f5f5f5;
  position: relative;
  padding-bottom: 70px;
}

.btn-back-logout {
  position: fixed;
  top: 20px;
  left: 20px;
  background: #409eff;
  color: white;
  border: none;
  padding: 10px 20px;
  border-radius: 4px;
  cursor: pointer;
  font-size: 14px;
  z-index: 1000;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.15);
  transition: background 0.3s;
}

.btn-back-logout:hover {
  background: #66b1ff;
}

.content {
  padding: 80px 20px 20px;
  max-width: 1200px;
  margin: 0 auto;
}

.bottom-nav {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  background: white;
  display: flex;
  justify-content: center;
  gap: 20px;
  padding: 15px;
  box-shadow: 0 -2px 8px rgba(0, 0, 0, 0.1);
  z-index: 1000;
}

.nav-btn {
  flex: 1;
  max-width: 200px;
  padding: 12px 24px;
  background: #f0f0f0;
  color: #666;
  text-decoration: none;
  border-radius: 8px;
  text-align: center;
  font-size: 16px;
  font-weight: 500;
  transition: all 0.3s;
}

.nav-btn:hover {
  background: #e0e0e0;
}

.nav-btn.active {
  background: #409eff;
  color: white;
  box-shadow: 0 2px 8px rgba(64, 158, 255, 0.3);
}
</style>
