<template>
  <div class="login-container">
    <div class="login-box">
      <h2>学生登录</h2>
      <form @submit.prevent="handleLogin">
        <div class="form-group">
          <label>学号</label>
          <input v-model="loginForm.account" type="text" placeholder="请输入学号" required />
        </div>
        <div class="form-group">
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
const loginForm = ref({
  account: '',
  password: ''
})
const errorMsg = ref('')

const handleLogin = async () => {
  try {
    const res = await request.post('/student/auth/login', loginForm.value)
    if (res.data.code === 200) {
      // 保存用户信息到本地存储
      localStorage.setItem('studentInfo', JSON.stringify(res.data.data))
      localStorage.setItem('studentId', res.data.data.studentId)
      
      // 设置过期时间为1天后（时间戳）
      const expireTime = Date.now() + 24 * 60 * 60 * 1000 // 24小时
      localStorage.setItem('loginExpireTime', expireTime.toString())
      
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
.login-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  background: #f5f5f5;
}

.login-box {
  background: white;
  padding: 40px;
  border-radius: 8px;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
  width: 350px;
}

h2 {
  text-align: center;
  margin-bottom: 30px;
  color: #333;
}

.form-group {
  margin-bottom: 20px;
}

label {
  display: block;
  margin-bottom: 8px;
  color: #666;
  font-size: 14px;
}

input {
  width: 100%;
  padding: 10px;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-size: 14px;
  box-sizing: border-box;
}

input:focus {
  outline: none;
  border-color: #409eff;
}

.btn-login {
  width: 100%;
  padding: 12px;
  background: #409eff;
  color: white;
  border: none;
  border-radius: 4px;
  font-size: 16px;
  cursor: pointer;
  margin-top: 10px;
}

.btn-login:hover {
  background: #66b1ff;
}

.error-msg {
  color: #f56c6c;
  text-align: center;
  margin-top: 15px;
  font-size: 14px;
}
</style>
