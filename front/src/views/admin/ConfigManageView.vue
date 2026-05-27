<template>
  <div class="config-manage">
    <h2 class="page-title">⚙️ 系统配置</h2>

    <div class="config-grid">
      <!-- 选题开关 -->
      <div class="config-card">
        <div class="card-header">
          <span class="card-icon">📋</span>
          <div>
            <div class="card-title">选题功能开关</div>
            <div class="card-desc">控制学生端是否可以选择论文题目</div>
          </div>
        </div>
        <div class="card-body">
          <label class="switch">
            <input type="checkbox" v-model="topicEnabled" @change="toggleTopic" />
            <span class="slider"></span>
          </label>
          <span class="switch-label">{{ topicEnabled ? '已开启' : '已关闭' }}</span>
        </div>
      </div>

      <!-- 组队开关 -->
      <div class="config-card">
        <div class="card-header">
          <span class="card-icon">🚩</span>
          <div>
            <div class="card-title">组队功能开关</div>
            <div class="card-desc">控制学生端是否可以组队</div>
          </div>
        </div>
        <div class="card-body">
          <label class="switch">
            <input type="checkbox" v-model="teamEnabled" @change="toggleTeam" />
            <span class="slider"></span>
          </label>
          <span class="switch-label">{{ teamEnabled ? '已开启' : '已关闭' }}</span>
        </div>
      </div>

      <!-- 人数限制 -->
      <div class="config-card">
        <div class="card-header">
          <span class="card-icon">👥</span>
          <div>
            <div class="card-title">队伍人数限制</div>
            <div class="card-desc">设置队伍的最小和最大成员数</div>
          </div>
        </div>
        <div class="card-body">
          <div class="limit-fields">
            <div class="limit-field">
              <label>最小人数</label>
              <input v-model.number="minMembers" type="number" min="1" />
            </div>
            <span class="limit-sep">~</span>
            <div class="limit-field">
              <label>最大人数</label>
              <input v-model.number="maxMembers" type="number" min="1" />
            </div>
          </div>
          <button class="btn btn-primary" @click="saveLimits" :disabled="saving">保存限制</button>
        </div>
      </div>

      <!-- 其他配置动态展示 -->
      <div class="config-card" v-for="cfg in otherConfigs" :key="cfg.configKey">
        <div class="card-header">
          <span class="card-icon">🔧</span>
          <div>
            <div class="card-title">{{ cfg.configDesc || cfg.configKey }}</div>
            <div class="card-desc">配置键：{{ cfg.configKey }}</div>
          </div>
        </div>
        <div class="card-body">
          <div class="config-value">当前值：<code>{{ cfg.configValue }}</code></div>
        </div>
      </div>
    </div>

    <!-- 消息提示 -->
    <div v-if="msg.text" :class="['toast', msg.type]">{{ msg.text }}</div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { request } from '@/api/request'

const configs = ref([])
const topicEnabled = ref(false)
const teamEnabled = ref(false)
const minMembers = ref(1)
const maxMembers = ref(5)
const saving = ref(false)
const msg = ref({ text: '', type: 'success' })

function showMessage(text, type = 'success') {
  msg.value = { text, type }
  setTimeout(() => msg.value = { text: '', type: 'success' }, 3000)
}

const otherConfigs = ref([])

async function fetchConfig() {
  try {
    const res = await request.get('/admin/config/get')
    if (res.data.code === 200) {
      configs.value = res.data.data || []
      configs.value.forEach(c => {
        if (c.configKey === 'topic_selection_enabled') topicEnabled.value = c.configValue === '1'
        else if (c.configKey === 'team_invite_enabled') teamEnabled.value = c.configValue === '1'
        else if (c.configKey === 'max_team_members') maxMembers.value = parseInt(c.configValue) || 5
        else if (c.configKey === 'min_team_members') minMembers.value = parseInt(c.configValue) || 1
      })
      otherConfigs.value = configs.value.filter(c =>
        !['topic_selection_enabled', 'team_invite_enabled', 'max_team_members', 'min_team_members'].includes(c.configKey)
      )
    }
  } catch (e) { console.error(e) }
}

async function toggleTopic() {
  try {
    const res = await request.put('/admin/config/topic-switch', null, {
      params: { status: topicEnabled.value ? 1 : 0 }
    })
    if (res.data.code === 200) {
      showMessage('选题开关已' + (topicEnabled.value ? '开启' : '关闭'))
    } else {
      topicEnabled.value = !topicEnabled.value
      showMessage(res.data.msg || '操作失败', 'error')
    }
  } catch (e) { topicEnabled.value = !topicEnabled.value; showMessage('网络错误', 'error') }
}

async function toggleTeam() {
  try {
    const res = await request.put('/admin/config/team-switch', null, {
      params: { status: teamEnabled.value ? 1 : 0 }
    })
    if (res.data.code === 200) {
      showMessage('组队开关已' + (teamEnabled.value ? '开启' : '关闭'))
    } else {
      teamEnabled.value = !teamEnabled.value
      showMessage(res.data.msg || '操作失败', 'error')
    }
  } catch (e) { teamEnabled.value = !teamEnabled.value; showMessage('网络错误', 'error') }
}

async function saveLimits() {
  saving.value = true
  try {
    const res = await request.put('/admin/config/team-limit', {
      minTeamMembers: minMembers.value,
      maxTeamMembers: maxMembers.value
    })
    if (res.data.code === 200) {
      showMessage('人数限制已保存')
    } else {
      showMessage(res.data.msg || '保存失败', 'error')
    }
  } catch (e) { showMessage('保存失败', 'error') }
  finally { saving.value = false }
}

onMounted(fetchConfig)
</script>

<style scoped>
.config-manage { max-width: 900px; }
.page-title { font-size: 18px; margin-bottom: 20px; color: #333; }
.config-grid { display: flex; flex-direction: column; gap: 16px; }
.config-card { background: #fff; border-radius: 10px; padding: 20px; box-shadow: 0 1px 4px rgba(0,0,0,0.06); }
.card-header { display: flex; align-items: flex-start; gap: 12px; margin-bottom: 16px; }
.card-icon { font-size: 28px; }
.card-title { font-size: 15px; font-weight: 600; color: #333; }
.card-desc { font-size: 12px; color: #999; margin-top: 2px; }
.card-body { display: flex; align-items: center; gap: 16px; margin-left: 40px; flex-wrap: wrap; }

.switch { position: relative; display: inline-block; width: 48px; height: 26px; flex-shrink: 0; }
.switch input { opacity: 0; width: 0; height: 0; }
.slider { position: absolute; cursor: pointer; inset: 0; background: #ccc; border-radius: 26px; transition: 0.3s; }
.slider::before { content: ''; position: absolute; height: 20px; width: 20px; left: 3px; bottom: 3px; background: #fff; border-radius: 50%; transition: 0.3s; }
.switch input:checked + .slider { background: #2e7d32; }
.switch input:checked + .slider::before { transform: translateX(22px); }
.switch-label { font-size: 14px; color: #666; }

.limit-fields { display: flex; align-items: flex-end; gap: 8px; }
.limit-field { display: flex; flex-direction: column; gap: 4px; }
.limit-field label { font-size: 12px; color: #888; }
.limit-field input { width: 70px; padding: 8px; border: 1px solid #ddd; border-radius: 6px; text-align: center; font-size: 14px; outline: none; }
.limit-field input:focus { border-color: #302b63; }
.limit-sep { font-size: 18px; color: #999; padding-bottom: 8px; }
.config-value { font-size: 14px; color: #666; }
.config-value code { background: #f5f5f5; padding: 2px 8px; border-radius: 4px; font-size: 13px; }

.btn { padding: 8px 20px; border: 1px solid #ddd; border-radius: 6px; cursor: pointer; font-size: 13px; background: #fff; }
.btn-primary { background: #1a1a2e; color: #fff; border-color: #1a1a2e; }
.btn:disabled { opacity: 0.6; cursor: not-allowed; }
.toast { padding: 10px 16px; border-radius: 8px; margin-bottom: 12px; font-size: 13px; }
.toast.success { background: #e8f5e9; color: #2e7d32; border: 1px solid #a5d6a7; }
.toast.error { background: #fbe9e7; color: #c62828; border: 1px solid #ef9a9a; }
</style>
