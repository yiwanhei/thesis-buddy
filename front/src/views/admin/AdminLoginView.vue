<template>
  <div class="login-page">
    <div class="login-card">
      <div class="login-header">
        <h1>📚</h1>
        <h2>论文选题系统</h2>
        <p class="subtitle">管理员后台</p>
      </div>
      <form @submit.prevent="handleLogin" class="login-form">
        <div class="form-group">
          <label>账号</label>
          <input v-model="account" type="text" placeholder="请输入管理员账号" required />
        </div>
        <div class="form-group">
          <label>密码</label>
          <input v-model="password" type="password" placeholder="请输入密码" required />
        </div>
        <p v-if="errorMsg" class="error-msg">{{ errorMsg }}</p>
        <button type="submit" class="login-btn" :disabled="loading">
          {{ loading ? '登录中...' : '登 录' }}
        </button>
      </form>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { request } from '@/api/request'

const router = useRouter()
const account = ref('')
const password = ref('')
const errorMsg = ref('')
const loading = ref(false)

async function handleLogin() {
  errorMsg.value = ''
  loading.value = true
  try {
    const res = await request.post('/admin/auth/login', {
      account: account.value,
      password: password.value
    })
    if (res.data.code === 200) {
      localStorage.setItem('adminToken', res.data.data.token)
      localStorage.setItem('adminInfo', JSON.stringify(res.data.data.admin))
      router.push('/admin/dashboard')
    } else {
      errorMsg.value = res.data.msg || '登录失败'
    }
  } catch (e) {
    errorMsg.value = '网络错误，请检查后端是否启动'
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #0f0c29, #302b63, #24243e);
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
}

.login-card {
  background: rgba(255, 255, 255, 0.95);
  border-radius: 16px;
  padding: 48px 40px;
  width: 400px;
  max-width: 90vw;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.3);
}

.login-header {
  text-align: center;
  margin-bottom: 32px;
}

.login-header h1 { font-size: 48px; margin-bottom: 8px; }
.login-header h2 { font-size: 20px; color: #1a1a2e; margin-bottom: 4px; }
.login-header .subtitle { font-size: 14px; color: #888; }

.login-form .form-group {
  margin-bottom: 20px;
}

.login-form label {
  display: block;
  font-size: 14px;
  color: #333;
  margin-bottom: 6px;
  font-weight: 500;
}

.login-form input {
  width: 100%;
  padding: 12px 14px;
  border: 1px solid #ddd;
  border-radius: 8px;
  font-size: 15px;
  outline: none;
  transition: border-color 0.2s;
  box-sizing: border-box;
}

.login-form input:focus {
  border-color: #302b63;
}

.error-msg {
  color: #e94560;
  font-size: 13px;
  margin-bottom: 10px;
}

.login-btn {
  width: 100%;
  padding: 12px;
  background: linear-gradient(135deg, #302b63, #24243e);
  color: #fff;
  border: none;
  border-radius: 8px;
  font-size: 16px;
  font-weight: 600;
  cursor: pointer;
  transition: opacity 0.2s;
}

.login-btn:hover { opacity: 0.9; }
.login-btn:disabled { opacity: 0.6; cursor: not-allowed; }
</style>
