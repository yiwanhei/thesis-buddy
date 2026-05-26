<template>
  <div class="app-page">
    <h2>申请选题</h2>

    <!-- 已有预占记录 -->
    <div v-if="existingApp" class="section">
      <div class="status-card">
        <p><strong>当前状态：</strong>
          <span class="tag" :class="statusClass">{{ statusText }}</span>
        </p>
        <p><strong>选题：</strong>{{ topicInfo?.topicName || '—' }}</p>
        <p v-if="existingApp.applyType === 'team'"><strong>类型：</strong>多人选题</p>
        <p v-if="existingApp.applyReason"><strong>申请理由：</strong>{{ existingApp.applyReason }}</p>
        <p v-if="existingApp.reserveUntil"><strong>名额保留至：</strong>{{ formatTime(existingApp.reserveUntil) }}</p>
      </div>

      <div v-if="existingApp.applicationStatus === 1" class="actions">
        <button @click="handleConfirm" class="btn-confirm">确认提交审核</button>
        <button @click="handleCancel" class="btn-cancel">取消申请</button>
      </div>
      <div v-else class="actions">
        <button @click="$router.push('/topics')" class="btn-back">返回选题列表</button>
      </div>
    </div>

    <!-- 新申请表单 -->
    <div v-else class="section">
      <div v-if="topicInfo" class="topic-info">
        <p><strong>{{ topicInfo.topicName }}</strong></p>
        <p>类别：{{ topicInfo.category }} | 剩余名额：{{ topicInfo.maxCapacity }}</p>
      </div>

      <div class="field">
        <label>申请理由 <span class="red">*</span></label>
        <textarea v-model="formData.applyReason" rows="4" placeholder="请说明选题理由..." required></textarea>
      </div>
      <div class="field">
        <label>成果形式</label>
        <div class="chk-group">
          <label v-for="f in resultForms" :key="f" class="chk-item">
            <input type="checkbox" :value="f" v-model="formData.selectedResultForms" /> {{ f }}
          </label>
        </div>
      </div>
      <div class="field">
        <label>指导教师 <span class="red">*</span></label>
        <select v-model="formData.teacherName" required>
          <option value="">请选择</option>
          <option v-for="t in teachers" :key="t.teacherId" :value="t.realName">{{ t.realName }}</option>
        </select>
      </div>

      <!-- 多人选题：成员分工 -->
      <div v-if="applyType === 'team' && teamMembers.length" class="field">
        <label>成员分工 <span class="red">*</span></label>
        <div v-for="m in teamMembers" :key="m.studentId" class="task-row">
          <span class="task-name">{{ m.realName || m.studentId }}</span>
          <input v-model="m.task" placeholder="分工职责" />
        </div>
      </div>

      <div class="actions">
        <button @click="handleSubmit" class="btn-confirm">提交申请</button>
        <button @click="handleCancel" class="btn-cancel">取消</button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { request } from '@/api/request'
import { useRouter, useRoute } from 'vue-router'

const router = useRouter()
const route = useRoute()

const topicInfo = ref(null)
const applyType = ref('')
const resultForms = ref([])
const teachers = ref([])
const teamMembers = ref([])
const existingApp = ref(null)
const formData = ref({ selectedResultForms: [], applyReason: '', teacherName: '' })

const statusMap = { 1: '预占中', 2: '审核中', 3: '已通过', 0: '已取消' }
const statusClass = computed(() => ({ 1: 'tag-pending', 2: 'tag-review', 3: 'tag-done', 0: 'tag-cancel' }[existingApp.value?.applicationStatus] || ''))
const statusText = computed(() => statusMap[existingApp.value?.applicationStatus] || '未知')

// 检查现有预占
const checkReservation = async () => {
  const sid = localStorage.getItem('studentId')
  if (!sid) return
  try {
    const res = await request.get('/student/application/check-reservation', { params: { studentId: sid } })
    if (res.data.code === 200 && res.data.data?.hasReservation) {
      // 有预占，加载详情
      const detail = await request.get('/student/profile/application/my', { params: { studentId: sid } })
      if (detail.data.code === 200 && detail.data.data?.length) {
        existingApp.value = detail.data.data[0]
      }
    }
  } catch (e) { console.error(e) }
}

const loadTopicInfo = async () => {
  const topicId = route.query.topicId
  if (!topicId) { alert('缺少选题信息'); router.back(); return }
  try {
    const res = await request.get(`/student/topic/detail/${topicId}`)
    if (res.data.code === 200) {
      topicInfo.value = res.data.data.topic
      if (topicInfo.value?.resultForm) {
        resultForms.value = [...new Set(topicInfo.value.resultForm.split(/[或,]/).map(f => f.trim()).filter(Boolean))]
      }
    }
  } catch (e) { alert('加载选题失败') }
}

const loadTeachers = async () => {
  const sid = localStorage.getItem('studentId')
  if (!sid) return
  try {
    const res = await request.get('/student/profile/teachers', { params: { studentId: sid } })
    if (res.data.code === 200) teachers.value = res.data.data || []
  } catch (e) { console.error(e) }
}

const loadTeamInfo = async () => {
  const sid = localStorage.getItem('studentId')
  if (!sid) return
  try {
    const res = await request.get('/student/team/my-team', { params: { studentId: sid } })
    if (res.data.code === 200 && res.data.data) {
      teamMembers.value = (res.data.data.members || []).map(m => ({ studentId: m.studentId, realName: m.realName, task: '' }))
    }
  } catch (e) { console.error(e) }
}

const handleSubmit = async () => {
  const sid = localStorage.getItem('studentId')
  if (!sid) { alert('请先登录'); return }
  if (!formData.value.applyReason) { alert('请填写申请理由'); return }
  if (!formData.value.teacherName) { alert('请选择指导教师'); return }
  if (applyType.value === 'team' && teamMembers.value.some(m => !m.task?.trim())) { alert('请填写所有成员分工'); return }

  try {
    const data = {
      studentId: parseInt(sid), topicId: parseInt(route.query.topicId),
      applyType: applyType.value, applyReason: formData.value.applyReason,
      resultForm: formData.value.selectedResultForms.join(','),
      teacherName: formData.value.teacherName
    }
    if (applyType.value === 'team' && teamMembers.value.length) {
      data.teamId = teamMembers.value[0].teamId
      data.memberTasks = teamMembers.value.map(m => ({ studentId: m.studentId, task: m.task.trim() }))
    }
    const res = await request.post('/student/application/submit', data)
    if (res.data.code === 200) {
      alert(res.data.message || '申请成功！名额已保留30分钟')
      sessionStorage.setItem('needRefreshTopics', 'true')
      router.push('/topics')
    } else {
      alert(res.data.message || '申请失败')
    }
  } catch (e) { alert('网络错误') }
}

const handleConfirm = async () => {
  const sid = localStorage.getItem('studentId')
  if (!sid) return
  try {
    const res = await request.put('/student/application/confirm', null, {
      params: { studentId: sid, teamId: existingApp.value?.teamId || 0, topicId: existingApp.value?.topicId }
    })
    alert(res.data.message || '提交成功')
    router.push('/topics')
  } catch (e) { alert('提交失败') }
}

const handleCancel = () => {
  if (existingApp.value?.applicationId) {
    if (!confirm('确定取消申请吗？')) return
    request.delete(`/student/application/cancel/${existingApp.value.applicationId}`, {
      params: { studentId: localStorage.getItem('studentId') }
    }).then(res => {
      alert(res.data.message || '已取消')
      sessionStorage.setItem('needRefreshTopics', 'true')
      router.push('/topics')
    }).catch(() => alert('取消失败'))
  } else {
    router.back()
  }
}

const formatTime = (t) => t ? new Date(t).toLocaleString() : ''

onMounted(() => {
  applyType.value = route.query.type || 'individual'
  checkReservation()
  loadTopicInfo()
  loadTeachers()
  if (applyType.value === 'team') loadTeamInfo()
})
</script>

<style scoped>
.app-page { padding: 0 4px; }
.app-page h2 { font-size: 20px; color: #333; margin-bottom: 16px; }
.section { background: #fff; border-radius: 10px; padding: 16px; box-shadow: 0 1px 4px rgba(0,0,0,.08); }
.status-card p { font-size: 14px; color: #333; margin: 6px 0; }
.topic-info { background: #f0f9ff; padding: 12px; border-radius: 8px; margin-bottom: 16px; font-size: 14px; color: #333; }
.tag { display: inline-block; padding: 2px 10px; border-radius: 10px; font-size: 13px; font-weight: 500; }
.tag-pending { background: #fff3cd; color: #856404; }
.tag-review { background: #cce5ff; color: #004085; }
.tag-done { background: #d4edda; color: #155724; }
.tag-cancel { background: #f8d7da; color: #721c24; }

.field { margin-bottom: 16px; }
.field label { display: block; margin-bottom: 6px; font-size: 14px; color: #333; font-weight: 500; }
.red { color: #f56c6c; }
.field textarea, .field select, .task-row input { width: 100%; padding: 10px; border: 1px solid #ddd; border-radius: 8px; outline: none; }
.field textarea:focus, .field select:focus, .task-row input:focus { border-color: #409eff; }

.chk-group { display: flex; flex-wrap: wrap; gap: 10px; padding: 10px; border: 1px solid #ddd; border-radius: 8px; }
.chk-item { display: flex; align-items: center; gap: 4px; font-size: 14px; cursor: pointer; }

.task-row { display: flex; gap: 8px; align-items: center; margin-bottom: 8px; }
.task-name { min-width: 60px; font-size: 14px; font-weight: 500; color: #333; }
.task-row input { flex: 1; }

.actions { display: flex; gap: 12px; margin-top: 20px; }
.btn-confirm, .btn-cancel, .btn-back { flex: 1; padding: 12px; border: none; border-radius: 8px; font-size: 16px; }
.btn-confirm { background: #409eff; color: #fff; }
.btn-cancel { background: #909399; color: #fff; }
.btn-back { background: #67c23a; color: #fff; text-align: center; }
</style>
