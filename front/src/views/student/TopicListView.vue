<template>
  <div class="topic-list">
    <div v-if="!selectedType" class="center-buttons">
      <button @click="selectType('individual')" class="type-btn">个人</button>
      <button @click="handleTeamSelect" class="type-btn">多人</button>
    </div>

    <div v-if="selectedType === 'team' && teamMessage" class="team-msg">{{ teamMessage }}</div>

    <div v-if="selectedType" class="topics-section">
      <h3>{{ selectedType === 'individual' ? '个人选题' : '多人选题' }}</h3>
      <select v-model="selectedCategory" @change="loadTopics" class="filter">
        <option value="">全部类别</option>
        <option value="软件">软件</option>
        <option value="网络">网络</option>
        <option value="云计算">云计算</option>
        <option value="大数据">大数据</option>
        <option value="数字媒体">数字媒体</option>
        <option value="计算机">计算机</option>
      </select>

      <div v-if="loading" class="loading">加载中...</div>
      <div v-else-if="topics.length === 0" class="empty">暂无选题</div>
      <div v-else class="topic-cards">
        <div v-for="topic in topics" :key="topic.topicId" class="topic-card">
          <div class="card-header">{{ topic.topicName }}</div>
          <div class="card-body">
            <p><span>类别：</span>{{ topic.category }}</p>
            <p><span>成果形式：</span>{{ formatResultForm(topic.resultForm) }}</p>
            <p><span>剩余名额：</span>{{ getRemainingCount(topic) }}</p>
          </div>
          <button @click="applyTopic(topic)" class="btn-apply" :disabled="topic.applied || getRemainingCount(topic) <= 0">
            {{ topic.applied ? '已申请' : (getRemainingCount(topic) <= 0 ? '已满' : '申请选题') }}
          </button>
        </div>
      </div>

      <div v-if="total > 0" class="pagination">
        <button @click="prevPage" :disabled="currentPage === 1">上一页</button>
        <span>{{ currentPage }}/{{ totalPages }}页</span>
        <button @click="nextPage" :disabled="currentPage === totalPages">下一页</button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onActivated } from 'vue'
import { request } from '@/api/request'
import { useRouter } from 'vue-router'

const router = useRouter()
const topics = ref([])
const loading = ref(false)
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const selectedCategory = ref('')
const selectedType = ref('')
const teamMessage = ref('')

const totalPages = computed(() => Math.ceil(total.value / pageSize.value))

const selectType = (type) => { selectedType.value = type; currentPage.value = 1; void loadTopics() }

const handleTeamSelect = async () => {
  const studentId = localStorage.getItem('studentId')
  if (!studentId) return
  try {
    const res = await request.get('/student/team/my-team', { params: { studentId: parseInt(studentId) } })
    if (res.data.code === 200 && res.data.data) {
      if (res.data.data.team.captainId === parseInt(studentId)) {
        selectType('team')
        checkTeamStatus(res.data.data.team)
      } else alert('您是队员，请等待队长操作选题')
    } else {
      if (confirm('您还没有队伍，是否前往创建或加入队伍？')) void router.push('/team')
    }
  } catch (e) { alert('验证队伍状态失败') }
}

const checkTeamStatus = (team) => {
  if (!team) { teamMessage.value = '请先创建队伍'; return }
  teamMessage.value = team.status === 'forming' ? `队伍组建中 ${team.memberCount||0}/${team.maxMembers}` : ''
}

const loadTopics = async () => {
  loading.value = true
  try {
    const params = { page: currentPage.value, size: pageSize.value }
    if (selectedCategory.value) params.category = selectedCategory.value
    const res = await request.get('/student/topic/list', { params })
    if (res.data.code === 200) {
      topics.value = res.data.data.list || []
      total.value = res.data.data.total || 0
      checkAppliedStatus()
    }
  } catch (e) { console.error(e) } finally { loading.value = false }
}

const checkAppliedStatus = async () => {
  const studentId = localStorage.getItem('studentId')
  if (!studentId) return
  try {
    const res = await request.get('/student/profile/application/my', { params: { studentId: parseInt(studentId) } })
    if (res.data.code === 200 && res.data.data) {
      const ids = res.data.data.map(a => a.topicId)
      topics.value.forEach(t => { t.applied = ids.includes(t.topicId) })
    }
  } catch (e) { console.error(e) }
}

const getRemainingCount = (topic) => topic.remaining !== undefined ? topic.remaining : (topic.maxCapacity || 0)
const formatResultForm = (r) => r ? r.replace(/,/g, '或') : '无'
const applyTopic = (topic) => router.push({ path: '/application', query: { topicId: topic.topicId, type: selectedType.value } })
const prevPage = () => { if (currentPage.value > 1) { currentPage.value--; void loadTopics() } }
const nextPage = () => { if (currentPage.value < totalPages.value) { currentPage.value++; void loadTopics() } }

onMounted(() => { void loadTopics() })
onActivated(() => {
  if (sessionStorage.getItem('needRefreshTopics') === 'true') {
    sessionStorage.removeItem('needRefreshTopics'); void loadTopics()
  }
})

</script>

<style scoped>
.center-buttons { display: flex; gap: 16px; justify-content: center; margin: 60px 0; }
.type-btn {
  padding: 14px 40px; font-size: 17px; border: 2px solid #409eff;
  background: #fff; color: #409eff; border-radius: 10px; font-weight: 500;
}
.team-msg { background: #fff3cd; color: #856404; padding: 12px; border-radius: 8px; margin: 12px; text-align: center; font-size: 14px; }

.topics-section { padding: 0; }
.topics-section h3 { font-size: 18px; color: #333; margin-bottom: 12px; }
.filter { width: 100%; padding: 10px; border: 1px solid #ddd; border-radius: 8px; margin-bottom: 12px; }

.topic-cards { display: flex; flex-direction: column; gap: 12px; }
.topic-card { background: #fff; border-radius: 10px; padding: 16px; box-shadow: 0 1px 4px rgba(0,0,0,.08); }
.card-header { font-size: 16px; font-weight: 600; color: #409eff; margin-bottom: 10px; }
.card-body p { font-size: 14px; color: #666; margin: 4px 0; }
.card-body span { color: #333; }

.btn-apply { width: 100%; padding: 11px; background: #409eff; color: #fff; border: none; border-radius: 8px; margin-top: 12px; font-size: 15px; }
.btn-apply:disabled { background: #c0c4cc; }

.loading, .empty { text-align: center; padding: 40px; color: #999; font-size: 14px; }

.pagination { display: flex; justify-content: center; align-items: center; gap: 12px; margin-top: 16px; font-size: 14px; color: #666; }
.pagination button { padding: 8px 16px; border: 1px solid #ddd; background: #fff; border-radius: 6px; }
.pagination button:disabled { background: #f5f5f5; }
</style>
