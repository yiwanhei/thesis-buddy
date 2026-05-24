<template>
  <div class="application-detail">
    <h2>申请选题详情</h2>
    
    <!-- 选题信息 -->
    <div v-if="topicInfo" class="topic-info-section">
      <h3>{{ topicInfo.topicName }}</h3>
      <div class="info-row">
        <span><strong>类别：</strong>{{ topicInfo.category }}</span>
      </div>
      <div class="info-row">
        <span><strong>成果形式：</strong>{{ topicInfo.resultForm }}</span>
      </div>
      <div class="info-row">
        <span><strong>剩余名额：</strong>{{ topicInfo.maxCapacity }}</span>
      </div>
    </div>

    <!-- 申请表单 -->
    <div class="form-section">
      <h3>填写申请信息</h3>
      <form @submit.prevent="handleSubmit">
        <div class="form-group">
          <label>申请类型</label>
          <input :value="applyType === 'individual' ? '个人选题' : '多人选题'" disabled />
        </div>
        
        <div class="form-group">
          <label>选题名称</label>
          <input :value="topicInfo?.topicName" disabled />
        </div>

        <!-- 成果形式选择（个人和多人选题都需要，支持多选） -->
        <div v-if="applyType === 'individual' || (applyType === 'team' && teamInfo)" class="form-group">
          <label>成果形式 <span class="required">*</span></label>
          <div class="checkbox-group">
            <div v-for="form in resultForms" :key="form" class="checkbox-item">
              <input 
                type="checkbox" 
                :id="'form-' + form" 
                :value="form"
                v-model="formData.selectedResultForms"
              />
              <label :for="'form-' + form">{{ form }}</label>
            </div>
          </div>
          <p v-if="resultForms.length === 0" class="no-options">该选题未设置成果形式选项</p>
        </div>

        <!-- 多人选题：显示队伍信息 -->
        <div v-if="applyType === 'team' && teamInfo" class="form-group">
          <label>队伍名称</label>
          <input :value="teamInfo.teamName" disabled />
        </div>

        <div v-if="applyType === 'team' && teamInfo" class="form-group">
          <label>队长</label>
          <input :value="teamInfo.captainId" disabled />
        </div>

        <div v-if="applyType === 'team' && teamInfo" class="form-group">
          <label>队员人数</label>
          <input :value="`${teamInfo.memberCount || 0} / ${teamInfo.maxMembers}`" disabled />
        </div>

        <!-- 多人选题：成员分工填写 -->
        <div v-if="applyType === 'team' && teamInfo" class="form-group">
          <label>成员分工 <span class="required">*</span></label>
          <div class="member-tasks">
            <div v-for="member in teamMembers" :key="member.studentId" class="member-task-item">
              <span class="member-name">{{ member.realName || member.studentId }}</span>
              <input 
                v-model="member.task" 
                type="text" 
                placeholder="请输入该成员的分工职责"
                required
              />
            </div>
          </div>
        </div>

        <div class="form-group">
          <label>申请理由 <span class="required">*</span></label>
          <textarea v-model="formData.applyReason" rows="6" required placeholder="请详细说明您选择该选题的理由..."></textarea>
        </div>

        <!-- 指导教师信息 -->
        <div class="form-group">
          <label>指导教师 <span class="required">*</span></label>
          <select v-model="formData.teacherName" required>
            <option value="">请选择指导教师</option>
            <option v-for="teacher in teachers" :key="teacher.teacherId" :value="teacher.realName">
              {{ teacher.realName }} - {{ teacher.title || '教师' }}
            </option>
          </select>
        </div>

        <div class="form-actions">
          <button type="submit" class="btn-submit">确认提交</button>
          <button type="button" @click="handleCancel" class="btn-cancel">取消</button>
        </div>
      </form>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { request } from '@/api/request'
import { useRouter, useRoute } from 'vue-router'

const router = useRouter()
const route = useRoute()

const topicInfo = ref(null)
const applyType = ref('') // individual 或 team
const teamInfo = ref(null) // 多人选题时的队伍信息
const resultForms = ref([]) // 成果形式列表（从topic_library获取）
const teachers = ref([]) // 教师列表
const teamMembers = ref([]) // 队伍成员列表（用于分工填写）
const formData = ref({
  selectedResultForms: [], // 选中的成果形式列表（多选）
  applyReason: '',
  teacherName: '' // 指导教师姓名
})

// 加载选题信息
const loadTopicInfo = async () => {
  const topicId = route.query.topicId
  if (!topicId) {
    alert('缺少选题信息')
    router.back()
    return
  }

  try {
    // 修复：使用路径参数而不是查询参数
    const res = await request.get(`/student/topic/detail/${topicId}`)
    
    if (res.data.code === 200) {
      // 后端返回的数据结构是 { topic, remaining }
      topicInfo.value = res.data.data.topic
      
      // 从当前选题中获取成果形式列表（使用resultForm字段）
      if (topicInfo.value.resultForm) {
        // 先按"或"字分割，再按逗号分割，支持两种分隔符
        const forms = topicInfo.value.resultForm.split(/[或,]/).map(f => f.trim()).filter(f => f)
        // 去重
        resultForms.value = [...new Set(forms)]
      } else {
        resultForms.value = []
      }
    }
  } catch (error) {
    console.error('加载选题失败:', error)
    alert('加载选题信息失败')
  }
}

// 加载教师列表（只加载本班级的教师）
const loadTeachers = async () => {
  const studentId = localStorage.getItem('studentId')
  if (!studentId) {
    return
  }
  
  try {
    const res = await request.get('/student/profile/teachers', {
      params: { studentId: parseInt(studentId) }
    })
    
    if (res.data.code === 200) {
      teachers.value = res.data.data || []
    }
  } catch (error) {
    console.error('加载教师列表失败:', error)
  }
}

// 加载队伍信息（多人选题时）
const loadTeamInfo = async () => {
  const studentId = localStorage.getItem('studentId')
  if (!studentId) {
    alert('请先登录')
    router.push('/login')
    return
  }

  try {
    const res = await request.get('/student/team/my-team', {
      params: { studentId: parseInt(studentId) }
    })
    
    if (res.data.code === 200 && res.data.data) {
      teamInfo.value = {
        teamId: res.data.data.team.teamId, // 保存teamId
        teamName: res.data.data.team.teamName,
        captainId: res.data.data.team.captainId,
        maxMembers: res.data.data.team.maxMembers,
        memberCount: res.data.data.members ? res.data.data.members.length : 0
      }
      
      // 加载队伍成员列表，用于分工填写
      if (res.data.data.members && res.data.data.members.length > 0) {
        teamMembers.value = res.data.data.members.map(member => ({
          studentId: member.studentId,
          realName: member.realName, // 保存成员姓名
          task: '' // 初始化为空，等待用户填写
        }))
      } else {
        // 如果没有成员，至少包含队长自己
        teamMembers.value = [{
          studentId: res.data.data.team.captainId,
          realName: '', // 队长姓名待补充
          task: ''
        }]
      }
    } else {
      alert('您还没有创建或加入队伍，请先创建或加入队伍')
      router.back()
    }
  } catch (error) {
    console.error('加载队伍信息失败:', error)
    alert('加载队伍信息失败')
  }
}

// 提交申请
const handleSubmit = async () => {
  const studentId = localStorage.getItem('studentId')
  if (!studentId) {
    alert('请先登录')
    return
  }

  const topicId = route.query.topicId
  if (!topicId) {
    alert('缺少选题信息')
    return
  }

  // 验证必填字段
  if (!formData.value.applyReason) {
    alert('请填写申请理由')
    return
  }

  // 验证成果形式（个人和多人选题都需要，至少选择一个）
  if (!formData.value.selectedResultForms || formData.value.selectedResultForms.length === 0) {
    alert('请至少选择一个成果形式')
    return
  }

  // 验证指导教师
  if (!formData.value.teacherName) {
    alert('请选择指导教师')
    return
  }

  // 多人选题验证成员分工
  if (applyType.value === 'team') {
    const allTasksFilled = teamMembers.value.every(member => member.task && member.task.trim())
    if (!allTasksFilled) {
      alert('请填写所有成员的分工职责')
      return
    }
  }

  try {
    const submitData = {
      studentId: parseInt(studentId),
      topicId: parseInt(topicId),
      applyType: applyType.value,
      applyReason: formData.value.applyReason,
      // 将成果形式数组转换为逗号分隔的字符串
      resultForm: formData.value.selectedResultForms.join(','),
      teacherName: formData.value.teacherName
    }

    // 多人选题添加队伍ID和成员分工
    if (applyType.value === 'team') {
      if (!teamInfo.value || !teamInfo.value.teamId) {
        alert('未找到队伍信息')
        return
      }
      submitData.teamId = teamInfo.value.teamId
      // 添加成员分工信息
      submitData.memberTasks = teamMembers.value.map(member => ({
        studentId: member.studentId,
        task: member.task.trim()
      }))
    }

    const res = await request.post('/student/application/submit', submitData)
    
    if (res.data.code === 200) {
      // 如果是多人选题，尝试更新队伍状态为confirmed（非阻塞性操作）
      if (applyType.value === 'team' && submitData.teamId) {
        try {
          await request.put('/student/team/update-status', null, {
            params: { teamId: submitData.teamId, status: 'confirmed' }
          })
        } catch (error) {
          console.warn('更新队伍状态失败，但申请已成功:', error)
          // 不阻断用户流程，只是记录警告
        }
      }
      
      alert('申请提交成功！名额已保留至明天00:00')
      // 标记需要刷新选题列表
      sessionStorage.setItem('needRefreshTopics', 'true')
      router.push('/topics')
    } else {
      // 显示后端返回的错误信息（包括重复提交提示）
      alert(res.data.message || '申请失败')
    }
  } catch (error) {
    console.error('提交申请失败:', error)
    alert('网络错误，请稍后重试')
  }
}

// 取消
const handleCancel = () => {
  if (confirm('确定要取消申请吗？')) {
    router.back()
  }
}

onMounted(() => {
  applyType.value = route.query.type || 'individual'
  loadTopicInfo()
  loadTeachers() // 加载教师列表
  
  // 如果是多人选题，加载队伍信息
  if (applyType.value === 'team') {
    loadTeamInfo()
  }
})
</script>

<style scoped>
.application-detail {
  background: white;
  padding: 30px;
  border-radius: 8px;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
  max-width: 800px;
  margin: 0 auto;
}

h2 {
  margin-bottom: 30px;
  color: #333;
  font-size: 24px;
  border-bottom: 2px solid #409eff;
  padding-bottom: 10px;
}

h3 {
  margin: 20px 0 15px 0;
  color: #409eff;
  font-size: 18px;
}

/* 选题信息区域 */
.topic-info-section {
  background: #f5f7fa;
  padding: 20px;
  border-radius: 8px;
  margin-bottom: 30px;
}

.topic-info-section h3 {
  margin-top: 0;
  color: #333;
  font-size: 20px;
}

.info-row {
  margin: 12px 0;
  font-size: 14px;
  color: #666;
}

/* 表单区域 */
.form-section {
  margin-top: 30px;
}

.form-group {
  margin-bottom: 20px;
}

.form-group label {
  display: block;
  margin-bottom: 8px;
  color: #333;
  font-size: 14px;
  font-weight: 500;
}

.required {
  color: #f56c6c;
}

.form-group input,
.form-group textarea,
.form-group select {
  width: 100%;
  padding: 10px;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-size: 14px;
  box-sizing: border-box;
  font-family: inherit;
}

.form-group select {
  cursor: pointer;
  background: white;
}

.form-group input:disabled {
  background: #f5f5f5;
  color: #999;
}

.form-group textarea {
  resize: vertical;
}

/* 多选框样式 */
.checkbox-group {
  display: flex;
  flex-wrap: wrap;
  gap: 15px;
  padding: 12px;
  border: 1px solid #ddd;
  border-radius: 4px;
  background: #fafafa;
  min-height: 60px;
}

.checkbox-item {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
}

.checkbox-item input[type="checkbox"] {
  width: auto;
  margin: 0;
  cursor: pointer;
}

.checkbox-item label {
  margin: 0;
  cursor: pointer;
  font-size: 14px;
  color: #333;
  user-select: none;
}

.checkbox-item input[type="checkbox"]:checked + label {
  color: #409eff;
  font-weight: 500;
}

.no-options {
  margin: 8px 0 0 0;
  color: #999;
  font-size: 13px;
  font-style: italic;
}

/* 成员分工样式 */
.member-tasks {
  border: 1px solid #ddd;
  border-radius: 4px;
  padding: 15px;
  background: #f9f9f9;
}

.member-task-item {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 12px;
}

.member-task-item:last-child {
  margin-bottom: 0;
}

.member-name {
  min-width: 80px;
  font-size: 14px;
  color: #333;
  font-weight: 500;
}

.member-task-item input {
  flex: 1;
  padding: 8px 12px;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-size: 14px;
}

.form-actions {
  display: flex;
  gap: 15px;
  margin-top: 30px;
  justify-content: center;
}

.btn-submit,
.btn-cancel {
  padding: 12px 40px;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 16px;
  font-weight: 500;
  transition: all 0.3s;
}

.btn-submit {
  background: #409eff;
  color: white;
}

.btn-submit:hover {
  background: #66b1ff;
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(64, 158, 255, 0.3);
}

.btn-cancel {
  background: #909399;
  color: white;
}

.btn-cancel:hover {
  background: #a6a9ad;
}
</style>
