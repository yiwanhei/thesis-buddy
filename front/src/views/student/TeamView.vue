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
    
    <!-- 消息区域（三标签切换） -->
    <div class="section message-section">
      <h3>消息与邀请</h3>
      
      <!-- 标签栏 -->
      <div class="tabs">
        <button 
          :class="['tab-btn', { active: activeTab === 'invite' }]"
          @click="activeTab = 'invite'">
          邀请
          <span v-if="inviteCount > 0" class="badge">{{ inviteCount }}</span>
        </button>
        <button 
          :class="['tab-btn', { active: activeTab === 'apply' }]"
          @click="activeTab = 'apply'">
          申请
          <span v-if="applyCount > 0" class="badge">{{ applyCount }}</span>
        </button>
        <button 
          :class="['tab-btn', { active: activeTab === 'other' }]"
          @click="activeTab = 'other'">
          其他
        </button>
      </div>
      
      <!-- ====== 邀请 Tab ====== -->
      <div v-if="activeTab === 'invite'" class="tab-content">
        <!-- 队长：搜索邀请同学 + 已发邀请 -->
        <template v-if="isCaptain">
          <div class="invite-student">
            <h4>邀请同班同学</h4>
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
          <div class="sent-invites">
            <h4>已发送的邀请</h4>
            <div v-if="sentInvites.length === 0" class="empty-tip">暂无已发送邀请</div>
            <div v-else class="invite-items">
              <div v-for="invite in sentInvites" :key="invite.invite_id" class="invite-item">
                <p>
                  已邀请 <strong>{{ invite.inviteeName || invite.inviteeStudentId }}</strong>
                  <span class="invite-status" :class="invite.status">
                    {{ invite.status === 'pending' ? '待回应' : invite.status === 'accepted' ? '已接受' : '已拒绝' }}
                  </span>
                </p>
              </div>
            </div>
          </div>
        </template>
        <!-- 单人学生：收到的邀请 -->
        <template v-else-if="!myTeam">
          <div class="received-invites">
            <h4>收到的邀请</h4>
            <div v-if="receivedInvites.length === 0" class="empty-tip">暂无邀请</div>
            <div v-else class="invite-items">
              <div v-for="invite in receivedInvites" :key="invite.invite_id" class="invite-item">
                <div class="invite-info">
                  <p>{{ formatInviteContent(invite) }}</p>
                </div>
                <div class="invite-actions">
                  <button @click="handleInvite(invite, 'accept')" class="btn-accept">接受</button>
                  <button @click="handleInvite(invite, 'reject')" class="btn-reject">拒绝</button>
                </div>
              </div>
            </div>
          </div>
        </template>
        <!-- 队员：不可被邀请 -->
        <template v-else>
          <div class="empty-tip">暂无消息</div>
        </template>
      </div>
      
      <!-- ====== 申请 Tab ====== -->
      <div v-if="activeTab === 'apply'" class="tab-content">
        <!-- 队长：收到的加入申请 -->
        <template v-if="isCaptain">
          <div class="request-list">
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
        </template>
        <!-- 单人学生：自己提交的申请 -->
        <template v-else-if="!myTeam">
          <div class="my-join-requests">
            <h4>我的申请</h4>
            <div v-if="myJoinRequests.length === 0" class="empty-tip">暂无申请</div>
            <div v-else class="request-items">
              <div v-for="req in myJoinRequests" :key="req.id" class="request-item my-request">
                <div class="request-info">
                  <p><strong>队伍：</strong>{{ req.teamName || req.team_id }}</p>
                  <p v-if="req.teamTopic"><strong>选题：</strong>{{ req.teamTopic }}</p>
                  <p><strong>队长：</strong>{{ req.captainName }}</p>
                  <p><strong>状态：</strong>
                    <span :class="'status-' + (req.status || 'pending')">
                      {{ req.status === 'accepted' ? '已同意' : req.status === 'rejected' ? '已拒绝' : '等待审核' }}
                    </span>
                  </p>
                </div>
              </div>
            </div>
          </div>
        </template>
        <!-- 队员 -->
        <template v-else>
          <div class="empty-tip">暂无消息</div>
        </template>
      </div>
      
      <!-- ====== 其他 Tab ====== -->
      <div v-if="activeTab === 'other'" class="tab-content">
        <div class="empty-tip">暂无系统通知</div>
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
                {{ topic.topicName }} (剩余名额: {{ topic.availableSlots }})
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
const sentInvites = ref([])
const myJoinRequests = ref([])
const activeTab = ref('invite')
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

const inviteCount = computed(() => {
  if (isCaptain.value) {
    return sentInvites.value.filter(i => i.status === 'pending').length
  } else if (!myTeam.value) {
    return receivedInvites.value.length
  }
  return 0
})

const applyCount = computed(() => {
  if (isCaptain.value) {
    return joinRequests.value.length
  } else if (!myTeam.value) {
    return myJoinRequests.value.length
  }
  return 0
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
        loadSentInvites()
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

const loadSentInvites = async () => {
  if (!myTeam.value) return
  
  try {
    const res = await request.get('/student/team/sent-invites', {
      params: { teamId: myTeam.value.teamId }
    })

    if (res.data.code === 200) {
      sentInvites.value = res.data.data || []
    }
  } catch (error) {
    console.error('加载已发邀请失败:', error)
  }
}

const loadMyJoinRequests = async () => {
  const studentId = localStorage.getItem('studentId')
  if (!studentId) return
  
  try {
    const res = await request.get('/student/team/my-join-requests', {
      params: { studentId: parseInt(studentId) }
    })

    if (res.data.code === 200) {
      myJoinRequests.value = res.data.data || []
    }
  } catch (error) {
    console.error('加载我的加入申请失败:', error)
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
  if (action === 'accept') {
    // 检查是否有单人预占记录（如果有，提示将清除）
    try {
      const checkRes = await request.get('/student/application/check-reservation', {
        params: { studentId: currentStudentId.value }
      })
      if (checkRes.data.data && checkRes.data.data.hasReservation) {
        if (!confirm('您当前有单人选题预占记录，接受邀请后将清除该预占。是否继续？')) {
          return
        }
      }
    } catch (e) {
      // 接口不存在则忽略
    }
  }
  
  try {
    const res = await request.put('/student/team/invite/handle', null, {
      params: { inviteId: (invite.invite_id || invite.inviteId), action }
    })

    if (res.data.code === 200) {
      if (action === 'accept') {
        alert('已接受邀请')
        loadMyTeam()
      } else {
        alert('已拒绝')
      }
      receivedInvites.value = receivedInvites.value.filter(i => (i.invite_id || i.inviteId) !== (invite.invite_id || invite.inviteId))
    } else {
      alert(res.data.message || '操作失败')
    }
  } catch (error) {
    alert('网络错误，请稍后重试')
  }
}

const formatInviteContent = (invite) => {
  // 增强显示：队长名 + 选题名
  const inviterName = invite.inviterName || `学生 ${invite.inviter_id || invite.inviterId}`
  const topicText = invite.teamTopic ? `（选题：${invite.teamTopic}）` : ''
  return `${inviterName} 邀请您加入队伍 ${topicText}`
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
  loadReceivedInvites() // 单人学生：加载收到的邀请
  loadMyJoinRequests() // 单人学生：加载自己提交的加入申请
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

/* 标签样式 */
.tabs {
  display: flex;
  gap: 0;
  margin-bottom: 20px;
  border-bottom: 2px solid #e0e0e0;
}

.tab-btn {
  padding: 10px 20px;
  background: none;
  border: none;
  border-bottom: 2px solid transparent;
  margin-bottom: -2px;
  cursor: pointer;
  font-size: 14px;
  color: #666;
  position: relative;
  transition: all 0.3s;
}

.tab-btn:hover {
  color: #409eff;
}

.tab-btn.active {
  color: #409eff;
  border-bottom-color: #409eff;
  font-weight: 500;
}

.badge {
  display: inline-block;
  min-width: 18px;
  height: 18px;
  padding: 0 5px;
  background: #f56c6c;
  color: white;
  border-radius: 9px;
  font-size: 12px;
  line-height: 18px;
  text-align: center;
  margin-left: 5px;
}

.tab-content {
  min-height: 100px;
}

/* 已发邀请 */
.sent-invites {
  margin-top: 20px;
  padding-top: 20px;
  border-top: 1px solid #e0e0e0;
}

.invite-status {
  font-size: 12px;
  padding: 2px 8px;
  border-radius: 4px;
  margin-left: 8px;
}

.invite-status.pending {
  background: #fff3cd;
  color: #856404;
}

.invite-status.accepted {
  background: #d4edda;
  color: #155724;
}

.invite-status.rejected {
  background: #f8d7da;
  color: #721c24;
}

/* 我的申请 */
.my-join-requests {
  margin-top: 10px;
}

.my-request {
  background: #f8f9fa;
}

.status-pending {
  color: #e6a23c;
  font-weight: 500;
}

.status-accepted {
  color: #67c23a;
  font-weight: 500;
}

.status-rejected {
  color: #f56c6c;
  font-weight: 500;
}

.received-invites .invite-item {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  padding: 12px;
  border: 1px solid #e0e0e0;
  border-radius: 8px;
  margin-bottom: 10px;
}

.invite-info {
  flex: 1;
}

.invite-info p {
  margin: 0;
  font-size: 14px;
  color: #333;
}

.sent-invites .invite-item {
  padding: 10px 0;
  border-bottom: 1px solid #f0f0f0;
}

.sent-invites .invite-item:last-child {
  border-bottom: none;
}

.sent-invites .invite-item p {
  margin: 0;
  font-size: 14px;
  color: #666;
}

@media (prefers-color-scheme: dark) {
  .tab-btn {
    color: #c0c4cc;
  }
  .tab-btn:hover,
  .tab-btn.active {
    color: #409eff;
  }
}
</style>