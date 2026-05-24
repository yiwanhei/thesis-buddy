<template>
  <div class="topic-list">
    <!-- 中央按钮区域 -->
    <div v-if="!selectedType" class="center-buttons">
      <button 
        @click="selectType('individual')" 
        :class="['type-btn', { active: selectedType === 'individual' }]"
      >
        个人
      </button>
      <button 
        @click="handleTeamSelect" 
        :class="['type-btn', { active: selectedType === 'team' }]"
      >
        多人
      </button>
    </div>
    
    <!-- 多人选题提示信息 -->
    <div v-if="selectedType === 'team' && teamMessage" class="team-message">
      <p>{{ teamMessage }}</p>
    </div>
    
    <!-- 倒计时提示 -->
    <div v-if="showCountdown" class="countdown-banner">
      <p>⏰ 申请选题后未确认提交保留一天名额，每天00:00刷新</p>
      <p class="countdown-time">剩余时间：{{ countdownText }}</p>
    </div>
    
    <!-- 选题列表区域 -->
    <div v-if="selectedType" class="topics-section">
      <h3>{{ selectedType === 'individual' ? '个人选题' : '多人选题' }}</h3>
      
      <!-- 筛选条件 -->
      <div class="filter-bar">
        <select v-model="selectedCategory" @change="loadTopics">
          <option value="">全部类别</option>
          <option value="软件">软件</option>
          <option value="网络">网络</option>
          <option value="云计算">云计算</option>
          <option value="大数据">大数据</option>
          <option value="数字媒体">数字媒体</option>
          <option value="计算机">计算机</option>
        </select>
      </div>

      <!-- 选题列表 -->
      <div v-if="loading" class="loading">加载中...</div>
      <div v-else-if="topics.length === 0" class="empty">暂无选题</div>
      <div v-else class="topic-cards">
        <div v-for="topic in topics" :key="topic.topicId" class="topic-card">
          <h3>{{ topic.topicName }}</h3>
          <div class="topic-info">
            <p><strong>选题名称：</strong>{{ topic.topicName }}</p>
            <p><strong>类别：</strong>{{ topic.category }}</p>
            <p><strong>成果形式：</strong>{{ formatResultForm(topic.resultForm) }}</p>
            <p><strong>剩余名额：</strong>{{ getRemainingCount(topic) }}</p>
          </div>
          <button @click="applyTopic(topic)" class="btn-apply" :disabled="topic.applied || getRemainingCount(topic) <= 0">
            {{ topic.applied ? '已申请' : (getRemainingCount(topic) <= 0 ? '已满' : '申请选题') }}
          </button>
        </div>
      </div>

      <!-- 分页 -->
      <div v-if="total > 0" class="pagination">
        <button @click="prevPage" :disabled="currentPage === 1">上一页</button>
        <span>第 {{ currentPage }} / {{ totalPages }} 页（共 {{ total }} 条）</span>
        <button @click="nextPage" :disabled="currentPage === totalPages">下一页</button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted, onActivated } from 'vue'
import { request } from '@/api/request'
import { useRouter } from 'vue-router'

const router = useRouter()
const topics = ref([])
const loading = ref(false)
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const selectedCategory = ref('')
const selectedType = ref('') // 新增：选择的类型（个人或多人）
const teamMessage = ref('') // 多人选题提示信息
const showCountdown = ref(false) // 是否显示倒计时
const countdownTime = ref(0) // 倒计时秒数
const countdownTimer = ref(null) // 定时器

const totalPages = computed(() => Math.ceil(total.value / pageSize.value))

// 倒计时文本
const countdownText = computed(() => {
  if (countdownTime.value <= 0) return '已释放名额'
  const hours = Math.floor(countdownTime.value / 3600)
  const minutes = Math.floor((countdownTime.value % 3600) / 60)
  const seconds = countdownTime.value % 60
  return `${hours.toString().padStart(2, '0')}:${minutes.toString().padStart(2, '0')}:${seconds.toString().padStart(2, '0')}`
})

// 选择类型
const selectType = (type) => {
  selectedType.value = type
  currentPage.value = 1
  loadTopics()
}

// 处理多人选题选择
const handleTeamSelect = async () => {
  const studentId = localStorage.getItem('studentId')
  if (!studentId) {
    alert('请先登录')
    return
  }

  try {
    // 检查学生队伍状态
    const res = await request.get('/student/team/my-team', {
      params: { studentId: parseInt(studentId) }
    })

    if (res.data.code === 200 && res.data.data) {
      const teamData = res.data.data
      const isCaptain = teamData.team.captainId === parseInt(studentId)
      
      if (isCaptain) {
        // 是队长，可以进入多人选题
        selectType('team')
        checkTeamStatus(teamData.team)
      } else {
        // 是队员，提示等待队长操作
        alert('您是队员，请等待队长操作选题')
      }
    } else {
      // 没有队伍，跳转到队伍页面
      if (confirm('您还没有加入任何队伍，是否前往创建或加入队伍？')) {
        router.push('/team')
      }
    }
  } catch (error) {
    console.error('检查队伍状态失败:', error)
    alert('无法验证您的队伍状态，请稍后重试')
  }
}

// 检查队伍状态并设置提示
const checkTeamStatus = (teamInfo) => {
  if (!teamInfo) {
    teamMessage.value = '您还没有创建队伍，请先创建队伍'
    return
  }

  // 根据队伍状态显示不同提示
  if (teamInfo.status === 'forming') {
    teamMessage.value = `您的队伍正在组建中，当前成员：${teamInfo.memberCount}/${teamInfo.maxMembers}`
  } else if (teamInfo.status === 'submitted') {
    teamMessage.value = '您的队伍已提交选题申请'
  } else {
    teamMessage.value = ''
  }
}

const loadTopics = async () => {
  loading.value = true
  try {
    const params = {
      page: currentPage.value,
      size: pageSize.value
    }
    if (selectedCategory.value) {
      params.category = selectedCategory.value
    }
    
    const res = await request.get('/student/topic/list', { params })
    if (res.data.code === 200) {
      topics.value = res.data.data.list || []
      total.value = res.data.data.total || 0
      
      // 检查当前学生是否已经申请过这些选题
      checkAppliedStatus()
    }
  } catch (error) {
    console.error('加载选题失败:', error)
  } finally {
    loading.value = false
  }
}

// 检查学生是否已经申请过某个选题
const checkAppliedStatus = async () => {
  const studentId = localStorage.getItem('studentId')
  if (!studentId) return
  
  try {
    const res = await request.get('/student/profile/application/my', {
      params: { studentId: parseInt(studentId) }
    })
    
    if (res.data.code === 200 && res.data.data) {
      // 获取学生已申请的选题ID列表
      const appliedTopicIds = res.data.data.map(app => app.topicId)
      
      // 标记已申请的选题
      topics.value.forEach(topic => {
        topic.applied = appliedTopicIds.includes(topic.topicId)
      })
    }
  } catch (error) {
    console.error('检查申请状态失败:', error)
  }
}

const getRemainingCount = (topic) => {
  // 使用后端返回的 remaining 字段（动态计算的剩余名额）
  return topic.remaining !== undefined ? topic.remaining : (topic.maxCapacity || 0)
}

// 格式化成果形式显示（用"或"字分隔）
const formatResultForm = (resultForm) => {
  if (!resultForm) return '无'
  // 如果是逗号分隔，转换为"或"字分隔
  return resultForm.replace(/,/g, '或')
}

const applyTopic = async (topic) => {
  const studentId = localStorage.getItem('studentId')
  if (!studentId) {
    alert('请先登录')
    return
  }

  // 跳转到申请详情页面（三级页面）
  router.push({
    path: '/application',
    query: {
      topicId: topic.topicId,
      type: selectedType.value
    }
  })
}

const prevPage = () => {
  if (currentPage.value > 1) {
    currentPage.value--
    loadTopics()
  }
}

const nextPage = () => {
  if (currentPage.value < totalPages.value) {
    currentPage.value++
    loadTopics()
  }
}

// 启动倒计时
const startCountdown = () => {
  // 计算到明天00:00的秒数
  const now = new Date()
  const tomorrow = new Date(now)
  tomorrow.setDate(tomorrow.getDate() + 1)
  tomorrow.setHours(0, 0, 0, 0)
  
  countdownTime.value = Math.floor((tomorrow - now) / 1000)
  showCountdown.value = true

  // 每秒更新
  countdownTimer.value = setInterval(() => {
    if (countdownTime.value > 0) {
      countdownTime.value--
    } else {
      // 倒计时结束，重新计算
      clearInterval(countdownTimer.value)
      startCountdown()
    }
  }, 1000)
}

onMounted(() => {
  loadTopics()
  startCountdown()
})

// 页面激活时重新加载数据（从其他页面返回时）
onActivated(() => {
  // 检查是否需要刷新选题列表
  const needRefresh = sessionStorage.getItem('needRefreshTopics')
  if (needRefresh === 'true') {
    sessionStorage.removeItem('needRefreshTopics')
    loadTopics() // 重新加载选题列表，更新剩余人数
  }
})

onUnmounted(() => {
  if (countdownTimer.value) {
    clearInterval(countdownTimer.value)
  }
})
</script>

<style scoped>
.topic-list {
  min-height: calc(100vh - 150px);
}

/* 中央按钮区域 */
.center-buttons {
  display: flex;
  justify-content: center;
  gap: 30px;
  margin: 40px 0;
}

.type-btn {
  padding: 15px 50px;
  font-size: 18px;
  font-weight: 500;
  border: 2px solid #409eff;
  background: white;
  color: #409eff;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.3s;
  min-width: 150px;
}

.type-btn:hover {
  background: #ecf5ff;
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(64, 158, 255, 0.3);
}

.type-btn.active {
  background: #409eff;
  color: white;
  box-shadow: 0 4px 12px rgba(64, 158, 255, 0.4);
}

/* 多人选题提示信息 */
.team-message {
  background: #fff3cd;
  border: 1px solid #ffc107;
  color: #856404;
  padding: 15px;
  border-radius: 8px;
  margin: 20px;
  text-align: center;
}

/* 倒计时横幅 */
.countdown-banner {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  padding: 20px;
  border-radius: 8px;
  margin: 20px;
  text-align: center;
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.3);
}

.countdown-banner p {
  margin: 5px 0;
  font-size: 16px;
}

.countdown-time {
  font-size: 24px;
  font-weight: bold;
  margin-top: 10px;
}

/* 选题列表区域 */
.topics-section {
  background: white;
  padding: 30px;
  border-radius: 8px;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
}

h3 {
  margin-bottom: 20px;
  color: #333;
  font-size: 20px;
}

.filter-bar {
  margin-bottom: 20px;
}

select {
  padding: 8px 12px;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-size: 14px;
}

.topic-cards {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 20px;
  margin-top: 20px;
}

.topic-card {
  border: 1px solid #e0e0e0;
  border-radius: 8px;
  padding: 20px;
  transition: box-shadow 0.3s;
}

.topic-card:hover {
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

.topic-card h3 {
  margin: 0 0 15px 0;
  color: #409eff;
  font-size: 18px;
}

.topic-info p {
  margin: 8px 0;
  font-size: 14px;
  color: #666;
}

.btn-apply {
  width: 100%;
  padding: 10px;
  background: #409eff;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  margin-top: 15px;
  font-size: 14px;
}

.btn-apply:hover:not(:disabled) {
  background: #66b1ff;
}

.btn-apply:disabled {
  background: #c0c4cc;
  cursor: not-allowed;
}

.loading, .empty {
  text-align: center;
  padding: 40px;
  color: #999;
}

.pagination {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 20px;
  margin-top: 30px;
}

.pagination button {
  padding: 8px 16px;
  border: 1px solid #ddd;
  background: white;
  border-radius: 4px;
  cursor: pointer;
}

.pagination button:disabled {
  background: #f5f5f5;
  cursor: not-allowed;
}

.pagination button:hover:not(:disabled) {
  background: #f0f0f0;
}
</style>
