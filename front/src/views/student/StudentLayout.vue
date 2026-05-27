<template>
  <div class="layout">
    <button @click="handleBackOrLogout" class="btn-back-logout">
      {{ showBackButton ? '返回' : '退出' }}
    </button>
    <main class="content">
      <router-view />
    </main>
    <nav class="bottom-nav">
      <router-link to="/topics" class="nav-btn" active-class="active">论文选题</router-link>
      <router-link to="/team" class="nav-btn" active-class="active">队伍</router-link>
      <router-link to="/application" class="nav-btn" active-class="active">申请</router-link>
    </nav>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'

const router = useRouter()
const route = useRoute()

const topLevelPages = ['/topics', '/team', '/application']

const showBackButton = computed(() => !topLevelPages.includes(route.path))

const handleBackOrLogout = () => {
  if (showBackButton.value) {
    router.back()
  } else {
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
  padding-bottom: 70px;
}

.btn-back-logout {
  position: fixed;
  top: 12px;
  left: 12px;
  background: #409eff;
  color: #fff;
  border: none;
  padding: 8px 16px;
  border-radius: 6px;
  font-size: 14px;
  z-index: 1000;
  box-shadow: 0 2px 8px rgba(0,0,0,.15);
}

.content {
  padding: 56px 12px 12px;
}

.bottom-nav {
  position: fixed;
  bottom: 0;
  left: 50%;
  transform: translateX(-50%);
  width: 100%;
  max-width: 480px;
  background: #fff;
  display: flex;
  justify-content: space-around;
  padding: 10px 0;
  padding-bottom: max(10px, env(safe-area-inset-bottom));
  box-shadow: 0 -2px 8px rgba(0,0,0,.1);
  z-index: 1000;
}

.nav-btn {
  flex: 1;
  text-align: center;
  padding: 10px 8px;
  color: #666;
  text-decoration: none;
  font-size: 15px;
  font-weight: 500;
  border-radius: 8px;
  margin: 0 6px;
  transition: all .2s;
}

.nav-btn.active {
  background: #409eff;
  color: #fff;
}
</style>
