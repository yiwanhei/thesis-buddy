<template>
  <div class="team-view">
    <!-- 中央按钮区域（未组队时显示） -->
    <div v-if="!myTeam" class="center-buttons">
      <button @click="showCreateForm = true" class="action-btn">
        创建队伍
      </button>
      <button @click="showJoinForm = true" class="action-btn">
        加入队伍
      </button>
    </div>
    
    <!-- 我的组队信息 -->
    <div v-if="myTeam" class="section">
      <h3>我的队伍</h3>
      <div class="team-info-card" @click="showTeamDetail = true">
        <p><strong>队长：</strong>{{ captainName }}</p>
        <p><strong>选题：</strong>{{ myTeam.applyTopic || '未选择' }}</p>
        <p><strong>成员数：</strong>{{ members.length }} / {{ myTeam.maxMembers }}</p>
        
        <!-- 名额保留倒计时 -->
        <div v-if="showReserveCountdown" class="reserve-countdown">
          <p>⏰ 保留名额倒计时：{{ reserveCountdownText }}</p>
          <p class="hint">请尽快提交选题</p>
        </div>
      </div>
    </div>
    
    <!-- 消息区域（所有学生可见，包括单人学生） -->
    <div class="section message-section">
      <h3>消息与邀请</h3>
      
      <!-- 队长专属：搜索并邀请同学 -->
      <div v-if="isCaptain" class="invite-student">
        <h4>邀请同班同学</h4>
        <p class="search-hint">搜索未提交申请的冋班同学</p>
        <div class="search-box">
          <input
            v-model="searchKeyword"
            type="text"
            placeholder="输入学号或姓名搜索"
            @input="handleSearch"
          />
          <button @click="handleSearch" class="btn-search">搜索</button>
        </div>

        <div v-if="searchResults.length > 0" class="search-results">
          <div v-for="student in searchResults" :key="student.student_id || student.studentId" class="student-item">
            <span>{{ student.real_name || student.realName }} ({{ student.student_id || student.studentId }})</span>
            <button @click="inviteStudent(student)" class="btn-invite">邀请</button>
          </div>
        </div>
      </div>
      
      <!-- 队长专属：加入请求列表 -->
      <div v-if="isCaptain" class="request-list">
        <h4>加入申请</h4>
        <div v-if="joinRequests.length === 0" class="empty-tip">暂无申请</div>
        <div v-else class="request-items">
          <div v-for="req in joinRequests" :key="req.id" class="request-item">
            <div class="request-info">
              <p><strong>申请人：</strong>{{ req.studentName }}</p>
              <p><strong>学号：</strong>{{ req.student_id }}</p>
              <p v-if="req.reason"><strong>申请理由：</strong>{{ req.reason }}</p>
              <p v-else><em>无申请理由</em></p>
            </div>
            <div class="request-actions">
              <button @click="handleJoinRequest(req, 'accept')" class="btn-accept">同意</button>
              <button @click="handleJoinRequest(req, 'reject')" class="btn-reject">拒绝</button>
            </div>
          </div>
        </div>
      </div>
      
      <!-- 所有学生可见：收到的邀请 -->
      <div class="invite-received">
        <h4>收到的邀请</h4>
        <div v-if="receivedInvites.length === 0" class="empty-tip">暂无邀请</div>
        <div v-else class="invite-items">
          <div v-for="invite in receivedInvites" :key="invite.inviteId" class="invite-item">
            <p>{{ formatInviteContent(invite) }}</p>
            <div class="invite-actions">
              <button @click="handleInvite(invite, 'accept')" class="btn-accept">接受</button>
              <button @click="handleInvite(invite, 'reject')" class="btn-reject">拒绝</button>
            </div>
          </div>
        </div>
      </div>
      
      <!-- 空状态提示 -->
      <div v-if="(!isCaptain || joinRequests.length === 0) && receivedInvites.length === 0" class="empty-tip">
        暂无消息
      </div>
    </div>

    <!-- 创建队伍表单 -->
    <div v-if="showCreateForm" class="modal-overlay" @click.self="showCreateForm = false">
      <div class="modal-content">
        <h3>创建队伍</h3>
        <form @submit.prevent="handleCreateTeam">
          <div class="form-group">
            <label>申请题目 <span class="required">*</span></label>
            <select
              v-model="createForm.topicId"
              required
              @change="onTopicSelect"
            >
              <option value="">请选择题目</option>
              <option
                v-for="topic in availableTopics"
                :key="topic.topicId"
                :value="topic.topicId"
              >
                {{ topic.topicName }} (剩余名额: {{ topic.availableSlots }}})
              </option>
            </select>
            <div v-if="selectedTopicInfo" class="topic-detail">
              <p><strong>类别：</strong>{{ selectedTopicInfo.category }}</p>
              <p><strong>成果形式：</strong>{{ selectedTopicInfo.resultForm }}</p>
            </div>
          </div>
          
          <div class="form-group">
            <label>预计人数 <span class="required">*</span></label>
            <div class="member-selector">
              <div 
                v-for="n in [2, 3, 4, 5]"
                :key="n"
                @click="selectMemberCount(n)"
                :class="['member-box', { 
                  selected: n === createForm.expectedMembers,
                  valid: isValidMemberCount,
                  invalid: !isValidMemberCount
                }]"
              >
                {{ n }}
              </div>
            </div>
            <p class="member-hint">最少2人，最多5人，创建后保留名额30分钟</p>
            <p v-if="topicCapacityHint" class="capacity-hint">{{ topicCapacityHint }}</p>

          </div>
          
          <div class="form-actions">
            <button 
              type="submit" 
              class="btn-submit" 
              :disabled="!canCreateTeam"
            >
              确认创建
            </button>
            <button type="button" @click="showCreateForm = false" class="btn-cancel">取消</button>
          </div>
        </form>
      </div>
    </div>

    <!-- 加入队伍表单 -->
    <div v-if="showJoinForm" class="modal-overlay" @click.self="showJoinForm = false">
      <div class="modal-content">
        <h3>加入队伍</h3>
        <form @submit.prevent="handleJoinTeam">
          <div class="form-group">
            <label>搜索队伍 <span class="required">*</span></label>
            <p class="search-hint">搜索缺人的队伍</p>
            <input
              v-model="joinForm.searchKeyword"
              type="text"
              placeholder="输入队长学号或队伍选题名称"
              @input="searchTeams"
            />
            <div v-if="searchedTeams.length > 0" class="team-search-results">
              <div
                v-for="team in searchedTeams"
                :key="team.teamId"
                class="team-option"
                :class="{ selected: joinForm.teamId === team.teamId }"
                @click="selectTeam(team)"
              >
                <p><strong>选题：</strong>{{ team.applyTopic }}</p>
                <p><strong>队长：</strong>{{ team.captainName }}</p>
                <p><strong>人数：</strong>{{ team.memberCount }} / {{ team.maxMembers }}</p>
              </div>
            </div>
          </div>
          <div class="form-group">
            <label>申请理由（选填）</label>
            <textarea v-model="joinForm.reason" rows="4" placeholder="可以填写申请理由，不是必填项"></textarea>
          </div>
          <div class="form-actions">
            <button type="submit" class="btn-submit" :disabled="!joinForm.teamId">申请加入</button>
            <button type="button" @click="showJoinForm = false" class="btn-cancel">取消</button>
          </div>
        </form>
      </div>
    </div>

    <!-- 队伍详情弹窗 -->
    <div v-if="showTeamDetail" class="modal-overlay" @click.self="closeTeamDetail">
      <div class="modal-content team-detail-modal">
        <h3>队伍详情</h3>
        <button @click="closeTeamDetail" class="btn-close-modal">×</button>
        
        <div class="team-detail-content">
          <div class="team-basic-info">
            <p><strong>选题：</strong>{{ myTeam.applyTopic || '未选择' }}</p>
            <p><strong>队长：</strong>{{ captainName }}</p>
            <p><strong>成员数：</strong>{{ members.length }} / {{ myTeam.maxMembers }}</p>
          </div>
          
          <div class="member-list">
            <h4>成员列表</h4>
            <ul>
              <li v-for="member in members" :key="member.memberId" class="member-item">
                <div class="member-info">
                  <span class="member-name">{{ member.realName || member.studentId }}</span>
                  <span class="member-role">{{ member.roleInTeam }}</span>
                </div>
                <div class="member-actions">
                  <input
                    v-if="!isLocked"
                    v-model="member.task"
                    type="text"
                    placeholder="输入职责分工"
                    @blur="updateMemberTask(member)"
                    class="task-input"
                  />
                  <span v-else class="task-text">{{ member.task || '未分配' }}</span>
                  <button
                    v-if="isCaptain && member.studentId !== currentStudentId && !isLocked"
                    @click="kickMember(member)"
                    class="btn-kick"
                  >
                    踢出
                  </button>
                </div>
              </li>
            </ul>
          </div>
          
          <div class="team-actions">
            <button 
              v-if="isCaptain && members.length === 1 && !isLocked"
              @click="dissolveTeam" 
              class="btn-dissolve"
            >
              解散队伍
            </button>
            <button 
              v-if="!isCaptain && !isLocked" 
              @click="quitTeam" 
              class="btn-quit"
            >
              退出队伍
            </button>
            <div v-if="isLocked" class="lock-notice">
              ⚠️ 队伍已提交申请，信息已锁定
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { request } from '@/api/request'

const currentStudentId = computed(() => {
  return parseInt(localStorage.getItem('studentId')) || 0
})

const myTeam = ref(null)
const members = ref([])
const showCreateForm = ref(false)
const showJoinForm = ref(false)
const showTeamDetail = ref(false)
const joinRequests = ref([])
const receivedInvites = ref([])
const reserveCountdownTime = ref(0)
const reserveTimer = ref(null)

// 搜索相关
const searchKeyword = ref('')
const searchResults = ref([])
const searchedTeams = ref([])

// 选题相关
const availableTopics = ref([])
const selectedTopicInfo = ref(null)

// 创建表单数据
const createForm = ref({
  topicId: '',
  expectedMembers: 2
})

// 加入表单数据
const joinForm = ref({
  searchKeyword: '',
  teamId: '',
  reason: ''
})

// 验证相关
const isValidMemberCount = ref(true)
const topicCapacityHint = ref('')

const isCaptain = computed(() => {
  return myTeam.value && myTeam.value.captainId === currentStudentId.value
})

const isLocked = computed(() => {
  return myTeam.value && myTeam.value.status === 'confirmed'
})

const captainName = computed(() => {
  if (!members.value || members.value.length === 0) return ''
  const captain = members.value.find(m => m.studentId === myTeam.value?.captainId)
  return captain?.realName || captain?.studentId || ''
})

const canCreateTeam = computed(() => {
  return createForm.value.topicId && isValidMemberCount.value
})

const showReserveCountdown = computed(() => {
  return myTeam.value && reserveCountdownTime.value > 0
})

const reserveCountdownText = computed(() => {
  if (reserveCountdownTime.value <= 0) return '已释放名额'
  const minutes = Math.floor(reserveCountdownTime.value / 60)
  const seconds = reserveCountdownTime.value % 60
  return `${minutes.toString().padStart(2, '0')}:${seconds.toString().padStart(2, '0')}`
})

const loadMyTeam = async () => {
  const studentId = localStorage.getItem('studentId')
  if (!studentId) return
  
  try {
    const res = await request.get('/student/team/my-team', {
      params: { studentId: parseInt(studentId) }
    })
    
    if (res.data.code === 200 && res.data.data) {
      myTeam.value = res.data.data.team
      members.value = res.data.data.members || []
      
      if (res.data.data.reserveUntil) {
        startReserveCountdown(res.data.data.reserveUntil)
      }

      if (isCaptain.value) {
        loadJoinRequests()
      }
    }
  } catch (error) {
    console.error('加载队伍信息失败:', error)
  }
}

const loadReceivedInvites = async () => {
  const studentId = localStorage.getItem('studentId')
  if (!studentId) return
  
  try {
    const res = await request.get('/student/team/invites', {
      params: { studentId: parseInt(studentId) }
    })

    if (res.data.code === 200) {
      receivedInvites.value = res.data.data || []
    }
  } catch (error) {
    console.error('加载邀请消息失败:', error)
  }
}

const loadJoinRequests = async () => {
  const teamId = myTeam.value?.teamId
  if (!teamId) return
  
  try {
    const res = await request.get('/student/team/join-requests', {
      params: { teamId }
    })

    if (res.data.code === 200) {
      joinRequests.value = res.data.data || []
    }
  } catch (error) {
    console.error('加载加入请求失败:', error)
  }
}

const selectMemberCount = (count) => {
  createForm.value.expectedMembers = count

  if (selectedTopicInfo.value) {
    if (count > selectedTopicInfo.value.availableSlots) {
      topicCapacityHint.value = `⚠️ 该题目仅剩 ${selectedTopicInfo.value.availableSlots} 个名额`
      isValidMemberCount.value = false
    } else {
      topicCapacityHint.value = `✅ 该题目有 ${selectedTopicInfo.value.availableSlots} 个可用名额`
      isValidMemberCount.value = true
    }
  } else {
    isValidMemberCount.value = count >= 2 && count <= 5
  }
}

const handleCreateTeam = async () => {
  if (!canCreateTeam.value) {
    alert('请完善队伍信息')
    return
  }

  const studentId = localStorage.getItem('studentId')
  if (!studentId) return
  
  try {
    const res = await request.post('/student/team/create', {
      captainId: parseInt(studentId),
      topicId: parseInt(createForm.value.topicId),
      expectedMembers: createForm.value.expectedMembers
    })
    
    if (res.data.code === 200) {
      alert('队伍创建成功！名额已保留30分钟，请尽快提交选题')
      showCreateForm.value = false
      createForm.value = {
        topicId: '',
        expectedMembers: 2
      }
      selectedTopicInfo.value = null
      topicCapacityHint.value = ''
      loadMyTeam()
      startReserveCountdown(30 * 60)
    } else {
      alert(res.data.message || '创建失败')
    }
  } catch (error) {
    alert('网络错误，请稍后重试')
  }
}

const searchTeams = async () => {
  const keyword = joinForm.value.searchKeyword.trim()
  if (!keyword) {
    searchedTeams.value = []
    return
  }
  
  const studentId = localStorage.getItem('studentId')
  if (!studentId) return
  
  try {
    const res = await request.get('/student/team/search', {
      params: { 
        keyword,
        studentId: parseInt(studentId)
      }
    })

    if (res.data.code === 200) {
      searchedTeams.value = res.data.data || []
    }
  } catch (error) {
    console.error('搜索队伍失败:', error)
  }
}

const selectTeam = (team) => {
  joinForm.value.teamId = team.teamId
}

const handleJoinTeam = async () => {
  if (!joinForm.value.teamId) {
    alert('请选择要加入的队伍')
    return
  }

  const studentId = localStorage.getItem('studentId')
  if (!studentId) return

  try {
    const res = await request.post('/student/team/join-request', {
      studentId: parseInt(studentId),
      teamId: parseInt(joinForm.value.teamId),
      reason: joinForm.value.reason
    })

    if (res.data.code === 200) {
      alert('申请已提交，等待队长审核')
      showJoinForm.value = false
      joinForm.value = { searchKeyword: '', teamId: '', reason: '' }
      searchedTeams.value = []
    } else {
      alert(res.data.message || '申请失败')
    }
  } catch (error) {
    alert('网络错误，请稍后重试')
  }
}

const handleJoinRequest = async (req, action) => {
  try {
    const res = await request.put('/student/team/join-request/handle', null, {
      params: {
        requestId: req.id,
        action,
        teamId: myTeam.value.teamId,
        captainId: currentStudentId.value
      }
    })
    
    if (res.data.code === 200) {
      if (action === 'accept') {
        alert('已同意加入')
        loadMyTeam()
      } else {
        alert('已拒绝')
      }
      loadJoinRequests()
    } else {
      alert(res.data.message || '操作失败')
    }
  } catch (error) {
    alert('网络错误，请稍后重试')
  }
}

const handleSearch = async () => {
  const keyword = searchKeyword.value.trim()
  if (!keyword) {
    searchResults.value = []
    return
  }
  
  const studentId = localStorage.getItem('studentId')
  if (!studentId) return
  
  try {
    const res = await request.get('/student/team/search-students', {
      params: { 
        keyword,
        studentId: parseInt(studentId)
      }
    })
    
    if (res.data.code === 200) {
      searchResults.value = res.data.data || []
    }
  } catch (error) {
    console.error('搜索学生失败:', error)
  }
}

const inviteStudent = async (student) => {
  try {
    const res = await request.post('/student/team/invite', {
      teamId: myTeam.value.teamId,
      inviterId: currentStudentId.value,
      inviteeId: student.student_id || student.studentId
    })

    if (res.data.code === 200) {
      alert('邀请已发送')
      searchKeyword.value = ''
      searchResults.value = []
    } else {
      alert(res.data.message || '邀请失败')
    }
  } catch (error) {
    alert('网络错误，请稍后重试')
  }
}

const handleInvite = async (invite, action) => {
  try {
    const res = await request.put('/student/team/invite/handle', null, {
      params: { inviteId: invite.inviteId, action }
    })

    if (res.data.code === 200) {
      if (action === 'accept') {
        alert('已接受邀请')
        loadMyTeam()
      } else {
        alert('已拒绝')
      }
      receivedInvites.value = receivedInvites.value.filter(i => i.inviteId !== invite.inviteId)
    } else {
      alert(res.data.message || '操作失败')
    }
  } catch (error) {
    alert('网络错误，请稍后重试')
  }
}

const formatInviteContent = (invite) => {
  // 根据邀请信息格式化显示内容
  // 可以显示为：队长XXX邀请您加入队伍（选题：XXX）
  return `学生 ${invite.inviterId} 邀请您加入队伍（队伍ID: ${invite.teamId}）`
}

const updateMemberTask = async (member) => {
  if (isLocked.value) return

  try {
    const res = await request.put('/student/team/member/task', {
      teamId: myTeam.value.teamId,
      studentId: member.studentId,
      task: member.task
    })

    if (res.data.code === 200) {
      console.log('职责更新成功')
    }
  } catch (error) {
    console.error('更新职责失败:', error)
    alert('更新职责失败')
  }
}

const kickMember = async (member) => {
  if (!confirm(`确定要踢出成员 ${member.realName || member.studentId} 吗？`)) {
    return
  }
  
  const studentId = localStorage.getItem('studentId')
  
  try {
    const res = await request.delete('/student/team/member/kick', {
      params: {
        teamId: myTeam.value.teamId,
        studentId: member.studentId,
        captainId: parseInt(studentId)
      }
    })
    
    if (res.data.code === 200) {
      alert('已踢出成员')
      loadMyTeam()
    } else {
      alert(res.data.message || '操作失败')
    }
  } catch (error) {
    alert('网络错误，请稍后重试')
  }
}

const dissolveTeam = async () => {
  if (!confirm('确定要解散队伍吗？此操作不可恢复！')) {
    return
  }
  
  const studentId = localStorage.getItem('studentId')
  try {
    const res = await request.delete('/student/team/dissolve', {
      params: { teamId: myTeam.value.teamId, captainId: parseInt(studentId) }
    })
    
    if (res.data.code === 200) {
      alert('队伍已解散')
      myTeam.value = null
      members.value = []
      showTeamDetail.value = false
    } else {
      alert(res.data.message || '解散失败')
    }
  } catch (error) {
    alert('网络错误，请稍后重试')
  }
}

const quitTeam = async () => {
  if (!myTeam.value) return
  
  if (!confirm('确定要退出队伍吗？')) {
    return
  }
  
  const studentId = localStorage.getItem('studentId')
  try {
    const res = await request.delete('/student/team/quit', {
      params: {
        teamId: myTeam.value.teamId,
        studentId: parseInt(studentId)
      }
    })
    
    if (res.data.code === 200) {
      alert('已退出队伍')
      myTeam.value = null
      members.value = []
      showTeamDetail.value = false
    } else {
      alert(res.data.message || '退出失败')
    }
  } catch (error) {
    alert('网络错误，请稍后重试')
  }
}

const startReserveCountdown = (seconds) => {
  reserveCountdownTime.value = seconds

  if (reserveTimer.value) {
    clearInterval(reserveTimer.value)
  }
  
  reserveTimer.value = setInterval(() => {
    if (reserveCountdownTime.value > 0) {
      reserveCountdownTime.value--
    } else {
      clearInterval(reserveTimer.value)
      alert('名额保留时间已到，已释放名额')
    }
  }, 1000)
}

const onTopicSelect = () => {
  const topicId = createForm.value.topicId
  if (!topicId) {
    selectedTopicInfo.value = null
    topicCapacityHint.value = ''
    return
  }

  const topic = availableTopics.value.find(t => t.topicId === parseInt(topicId))
  if (topic) {
    selectedTopicInfo.value = topic

    if (createForm.value.expectedMembers > topic.availableSlots) {
      topicCapacityHint.value = `⚠️ 该题目仅剩 ${topic.availableSlots} 个名额，请调整预计人数`
      isValidMemberCount.value = false
    } else {
      topicCapacityHint.value = `✅ 该题目有 ${topic.availableSlots} 个可用名额`
      isValidMemberCount.value = true
    }
  }
}

const loadAvailableTopics = async () => {
  try {
    const res = await request.get('/student/topic/available', {
      params: { status: 'available' }
    })

    if (res.data.code === 200) {
      availableTopics.value = res.data.data || []
    }
  } catch (error) {
    console.error('加载选题列表失败:', error)
  }
}

const closeTeamDetail = () => {
  showTeamDetail.value = false
}

onMounted(() => {
  loadMyTeam()
  loadAvailableTopics()
  loadReceivedInvites() // 加载邀请消息，所有学生都能看到
})

onUnmounted(() => {
  if (reserveTimer.value) {
    clearInterval(reserveTimer.value)
  }
})
</script>

<style scoped>
.team-view {
  min-height: calc(100vh - 150px);
  position: relative;
}

.center-buttons {
  display: flex;
  justify-content: center;
  gap: 30px;
  margin: 40px 0;
}

.action-btn {
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

.action-btn:hover {
  background: #ecf5ff;
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(64, 158, 255, 0.3);
}

.section {
  margin: 20px;
  padding: 20px;
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
}

h3 {
  margin-bottom: 20px;
  color: #409eff;
  font-size: 18px;
}

h4 {
  margin: 15px 0 10px 0;
  color: #333;
  font-size: 16px;
}

.empty-tip {
  text-align: center;
  padding: 40px;
  color: #999;
}

.team-info-card {
  border: 1px solid #e0e0e0;
  border-radius: 8px;
  padding: 20px;
  cursor: pointer;
  transition: all 0.3s;
}

.team-info-card:hover {
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
  transform: translateY(-2px);
}

.reserve-countdown {
  margin-top: 15px;
  padding: 15px;
  background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
  color: white;
  border-radius: 8px;
  text-align: center;
}

.reserve-countdown p {
  margin: 5px 0;
  font-size: 16px;
}

.reserve-countdown .hint {
  font-size: 14px;
  opacity: 0.9;
}

.team-info-card p {
  margin: 10px 0;
  font-size: 14px;
  color: #666;
}

/* 邀请和搜索样式 */
.invite-student {
  margin-bottom: 30px;
  padding-bottom: 20px;
  border-bottom: 1px solid #e0e0e0;
}

.search-hint {
  font-size: 12px;
  color: #999;
  margin: 5px 0 10px 0;
}

.search-box {
  display: flex;
  gap: 10px;
  margin-bottom: 15px;
}

.search-box input {
  flex: 1;
  padding: 10px;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-size: 14px;
}

.btn-search {
  padding: 10px 20px;
  background: #409eff;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
}

.btn-search:hover {
  background: #66b1ff;
}

.search-results {
  max-height: 300px;
  overflow-y: auto;
  border: 1px solid #e0e0e0;
  border-radius: 4px;
}

.student-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 10px 15px;
  border-bottom: 1px solid #f0f0f0;
}

.student-item:last-child {
  border-bottom: none;
}

.btn-invite {
  padding: 6px 16px;
  background: #67c23a;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 14px;
}

.btn-invite:hover {
  background: #85ce61;
}

/* 请求列表样式 */
.request-list {
  margin-top: 20px;
}

.request-items {
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.request-item {
  padding: 15px;
  border: 1px solid #e0e0e0;
  border-radius: 8px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.request-info p {
  margin: 5px 0;
  font-size: 14px;
  color: #666;
}

.request-actions {
  display: flex;
  gap: 10px;
}

.btn-accept,
.btn-reject {
  padding: 6px 16px;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 14px;
}

.btn-accept {
  background: #67c23a;
  color: white;
}

.btn-accept:hover {
  background: #85ce61;
}

.btn-reject {
  background: #f56c6c;
  color: white;
}

.btn-reject:hover {
  background: #f78989;
}

/* 队伍搜索样式 */
.team-search-results {
  max-height: 300px;
  overflow-y: auto;
  border: 1px solid #e0e0e0;
  border-radius: 4px;
  margin-top: 10px;
}

.team-option {
  padding: 12px 15px;
  border-bottom: 1px solid #f0f0f0;
  cursor: pointer;
  transition: background 0.3s;
}

.team-option:hover {
  background: #f5f7fa;
}

.team-option.selected {
  background: #ecf5ff;
  border-left: 3px solid #409eff;
}

.team-option p {
  margin: 5px 0;
  font-size: 14px;
  color: #666;
}

/* 队伍详情样式 */
.team-detail-modal {
  position: relative;
  min-width: 500px;
  max-width: 700px;
}

.btn-close-modal {
  position: absolute;
  top: 15px;
  right: 15px;
  background: none;
  border: none;
  font-size: 28px;
  color: #999;
  cursor: pointer;
  width: 30px;
  height: 30px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  transition: all 0.3s;
}

.btn-close-modal:hover {
  background: #f0f0f0;
  color: #333;
}

.team-detail-content {
  padding: 10px 0;
}

.team-basic-info {
  background: #f5f7fa;
  padding: 15px;
  border-radius: 8px;
  margin-bottom: 20px;
}

.team-basic-info p {
  margin: 10px 0;
  font-size: 14px;
  color: #666;
}

/* 成员列表样式 */
.member-list {
  margin: 20px 0;
}

.member-list ul {
  list-style: none;
  padding: 0;
}

.member-item {
  padding: 12px;
  border-bottom: 1px solid #f0f0f0;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.member-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 5px;
}

.member-name {
  font-size: 14px;
  color: #333;
  font-weight: 500;
}

.member-role {
  font-size: 12px;
  color: #999;
}

.member-actions {
  display: flex;
  align-items: center;
  gap: 10px;
}

.task-input {
  padding: 6px 10px;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-size: 13px;
  width: 200px;
}

.task-text {
  font-size: 13px;
  color: #666;
  padding: 6px 10px;
  background: #f5f7fa;
  border-radius: 4px;
}

.btn-kick {
  padding: 4px 12px;
  background: #f56c6c;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 12px;
}

.btn-kick:hover {
  background: #f78989;
}

/* 队伍操作按钮 */
.team-actions {
  margin-top: 20px;
  padding-top: 20px;
  border-top: 1px solid #e0e0e0;
  display: flex;
  gap: 10px;
  justify-content: center;
}

.btn-dissolve {
  padding: 10px 24px;
  background: #f56c6c;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 14px;
}

.btn-dissolve:hover {
  background: #f78989;
}

.btn-quit {
  padding: 10px 24px;
  background: #909399;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 14px;
}

.btn-quit:hover {
  background: #a6a9ad;
}

.locked-hint {
  color: #909399;
  font-size: 12px;
  padding: 4px 8px;
  background: #f5f5f5;
  border-radius: 4px;
}

.lock-notice {
  margin-top: 15px;
  padding: 12px;
  background: #fff3cd;
  border: 1px solid #ffc107;
  color: #856404;
  border-radius: 4px;
  text-align: center;
  font-size: 14px;
}

/* 表单样式 */
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 1000;
}

.modal-content {
  background: white;
  padding: 30px;
  border-radius: 12px;
  min-width: 400px;
  max-width: 600px;
  max-height: 80vh;
  overflow-y: auto;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.2);
  position: relative;
  z-index: 1001;
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
}

.form-group select {
  cursor: pointer;
  background: white;
}

.topic-detail {
  margin-top: 10px;
  padding: 12px;
  background: #f0f9ff;
  border-left: 3px solid #409eff;
  border-radius: 4px;
}

.topic-detail p {
  margin: 5px 0;
  font-size: 13px;
  color: #666;
}

.member-selector {
  display: flex;
  gap: 10px;
  margin: 10px 0;
}

.member-box {
  width: 40px;
  height: 40px;
  border: 2px solid #ddd;
  border-radius: 4px;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  font-weight: bold;
  transition: all 0.3s;
}

.member-box.selected {
  border-color: #409eff;
}

.member-box.valid {
  background: #67c23a;
  color: white;
  border-color: #67c23a;
}

.member-box.invalid {
  background: #f56c6c;
  color: white;
  border-color: #f56c6c;
}

.member-hint,
.capacity-hint {
  font-size: 12px;
  color: #999;
  margin-top: 5px;
}

.capacity-hint {
  color: #409eff;
}

.form-actions {
  display: flex;
  gap: 10px;
}

.btn-submit,
.btn-cancel {
  padding: 10px 24px;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 14px;
}

.btn-submit {
  background: #409eff;
  color: white;
}

.btn-submit:hover {
  background: #66b1ff;
}

.btn-submit:disabled {
  background: #c0c4cc;
  cursor: not-allowed;
}

.btn-cancel {
  background: #909399;
  color: white;
}

.btn-cancel:hover {
  background: #a6a9ad;
}
</style>