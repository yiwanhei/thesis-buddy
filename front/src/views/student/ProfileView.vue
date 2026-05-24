<template>
  <div class="profile-view">
    <h2>个人信息</h2>
    
    <div v-if="loading" class="loading">加载中...</div>
    <div v-else-if="studentInfo" class="profile-card">
      <div class="info-section">
        <h3>基本信息</h3>
        <div class="info-grid">
          <div class="info-item">
            <label>学号</label>
            <span>{{ studentInfo.account }}</span>
          </div>
          <div class="info-item">
            <label>姓名</label>
            <span>{{ studentInfo.realName }}</span>
          </div>
          <div class="info-item">
            <label>性别</label>
            <span>{{ studentInfo.gender || '-' }}</span>
          </div>
          <div class="info-item">
            <label>班级</label>
            <span>{{ classInfo?.className || '-' }}</span>
          </div>
          <div class="info-item">
            <label>手机号</label>
            <span>{{ studentInfo.phone || '-' }}</span>
          </div>
        </div>
      </div>

      <!-- 修改手机号 -->
      <div class="update-section">
        <h3>修改手机号</h3>
        <form @submit.prevent="handleUpdatePhone" class="update-form">
          <div class="form-group">
            <label>新手机号</label>
            <input v-model="phoneForm.phone" type="tel" placeholder="请输入新手机号" required />
          </div>
          <button type="submit" class="btn-update">更新</button>
        </form>
      </div>

      <!-- 修改密码 -->
      <div class="update-section">
        <h3>修改密码</h3>
        <form @submit.prevent="handleUpdatePassword" class="update-form">
          <div class="form-group">
            <label>旧密码</label>
            <input v-model="passwordForm.oldPassword" type="password" required />
          </div>
          <div class="form-group">
            <label>新密码</label>
            <input v-model="passwordForm.newPassword" type="password" required />
          </div>
          <div class="form-group">
            <label>确认新密码</label>
            <input v-model="passwordForm.confirmPassword" type="password" required />
          </div>
          <button type="submit" class="btn-update">更新密码</button>
        </form>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { request } from '@/api/request'

const studentInfo = ref(null)
const classInfo = ref(null)
const loading = ref(false)

const phoneForm = ref({
  phone: ''
})

const passwordForm = ref({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

const loadProfile = async () => {
  const studentId = localStorage.getItem('studentId')
  if (!studentId) return
  
  loading.value = true
  try {
    const res = await request.get('/student/profile/my', {
      params: { studentId: parseInt(studentId) }
    })
    
    if (res.data.code === 200) {
      studentInfo.value = res.data.data.student
      classInfo.value = res.data.data.classInfo
    }
  } catch (error) {
    console.error('加载个人信息失败:', error)
  } finally {
    loading.value = false
  }
}

const handleUpdatePhone = async () => {
  const studentId = localStorage.getItem('studentId')
  
  try {
    const res = await request.put('/student/profile/phone', {
      studentId: parseInt(studentId),
      phone: phoneForm.value.phone
    })
    
    if (res.data.code === 200) {
      alert('手机号修改成功')
      phoneForm.value.phone = ''
      loadProfile()
    } else {
      alert(res.data.message || '修改失败')
    }
  } catch (error) {
    alert('网络错误，请稍后重试')
  }
}

const handleUpdatePassword = async () => {
  if (passwordForm.value.newPassword !== passwordForm.value.confirmPassword) {
    alert('两次输入的新密码不一致')
    return
  }
  
  const studentId = localStorage.getItem('studentId')
  
  try {
    const res = await request.put('/student/auth/password', {
      studentId: parseInt(studentId),
      oldPassword: passwordForm.value.oldPassword,
      newPassword: passwordForm.value.newPassword
    })
    
    if (res.data.code === 200) {
      alert('密码修改成功')
      passwordForm.value = {
        oldPassword: '',
        newPassword: '',
        confirmPassword: ''
      }
    } else {
      alert(res.data.message || '修改失败')
    }
  } catch (error) {
    alert('网络错误，请稍后重试')
  }
}

onMounted(() => {
  loadProfile()
})
</script>

<style scoped>
.profile-view {
  background: white;
  padding: 30px;
  border-radius: 8px;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
}

h2 {
  margin-bottom: 30px;
  color: #333;
}

.loading {
  text-align: center;
  padding: 40px;
  color: #999;
}

.profile-card {
  max-width: 800px;
}

.info-section,
.update-section {
  margin-bottom: 40px;
  padding-bottom: 30px;
  border-bottom: 1px solid #e0e0e0;
}

.info-section:last-child,
.update-section:last-child {
  border-bottom: none;
}

h3 {
  margin-bottom: 20px;
  color: #409eff;
  font-size: 18px;
}

.info-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(250px, 1fr));
  gap: 20px;
}

.info-item {
  padding: 15px;
  background: #f5f7fa;
  border-radius: 4px;
}

.info-item label {
  display: block;
  font-size: 12px;
  color: #909399;
  margin-bottom: 5px;
}

.info-item span {
  font-size: 16px;
  color: #303133;
  font-weight: 500;
}

.update-form {
  max-width: 500px;
}

.form-group {
  margin-bottom: 20px;
}

.form-group label {
  display: block;
  margin-bottom: 8px;
  color: #666;
  font-size: 14px;
}

.form-group input {
  width: 100%;
  padding: 10px;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-size: 14px;
  box-sizing: border-box;
}

.form-group input:focus {
  outline: none;
  border-color: #409eff;
}

.btn-update {
  padding: 10px 24px;
  background: #409eff;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 14px;
}

.btn-update:hover {
  background: #66b1ff;
}
</style>
