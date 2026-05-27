<template>
  <div class="login-page">
    <div class="login-box">
      <h2>学生登录</h2>
      <form @submit.prevent="handleLogin">
        <div class="field">
          <label>学号</label>
          <input v-model="loginForm.account" type="text" placeholder="请输入学号" required />
        </div>
        <div class="field">
          <label>密码</label>
          <input v-model="loginForm.password" type="password" placeholder="请输入密码" required />
        </div>
        <button type="submit" class="btn-login">登录</button>
      </form>
      <p v-if="errorMsg" class="error-msg">{{ errorMsg }}</p>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { request } from '@/api/request'

const router = useRouter()
const loginForm = ref({ account: '', password: '' })
const errorMsg = ref('')

const handleLogin = async () => {
  try {
    const res = await request.post('/student/auth/login', loginForm.value)
    if (res.data.code === 200) {
      localStorage.setItem('studentInfo', JSON.stringify(res.data.data))
      localStorage.setItem('studentId', res.data.data.studentId)
      localStorage.setItem('loginExpireTime', (Date.now() + 86400000).toString())
      router.push('/topics')
    } else {
      errorMsg.value = res.data.message || '登录失败'
    }
  } catch (error) {
    errorMsg.value = '网络错误，请稍后重试'
  }
}
</script>

<style scoped>
.login-page {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  padding: 20px;
}

.login-box {
  background: #fff;
  padding: 32px 24px;
  border-radius: 12px;
  box-shadow: 0 2px 12px rgba(0,0,0,.1);
  width: 100%;
  max-width: 360px;
}

h2 {
  text-align: center;
  margin-bottom: 24px;
  font-size: 22px;
  color: #333;
}

.field {
  margin-bottom: 18px;
}
.field label {
  display: block;
  margin-bottom: 6px;
  color: #666;
  font-size: 14px;
}
.field input {
  width: 100%;
  padding: 12px;
  border: 1px solid #ddd;
  border-radius: 8px;
  outline: none;
}
.field input:focus {
  border-color: #409eff;
}

.btn-login {
  width: 100%;
  padding: 13px;
  background: #409eff;
  color: #fff;
  border: none;
  border-radius: 8px;
  font-size: 17px;
  margin-top: 8px;
}

.error-msg {
  color: #f56c6c;
  text-align: center;
  margin-top: 14px;
  font-size: 14px;
}
</style>
