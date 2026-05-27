<template>
  <div class="team-manage">
    <div class="toolbar">
      <h2 class="page-title">🚩 队伍管理</h2>
    </div>
    <div class="stat-bar">共 <strong>{{ total }}</strong> 个队伍</div>

    <!-- 消息提示 -->
    <div v-if="msg.text" :class="['toast', msg.type]">{{ msg.text }}</div>

    <div class="table-wrap">
      <table>
        <thead>
          <tr>
            <th>ID</th>
            <th>队名</th>
            <th>队长</th>
            <th>选题</th>
            <th>成员数</th>
            <th>状态</th>
            <th>创建时间</th>
            <th>操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="item in list" :key="item.team_id">
            <td>{{ item.team_id }}</td>
            <td>{{ item.team_name }}</td>
            <td>{{ item.captainName }}</td>
            <td>{{ item.apply_topic || '-' }}</td>
            <td>{{ item.memberCount }}/{{ item.max_members }}</td>
            <td>
              <span :class="['badge', item.status === 'forming' ? 'forming' : 'confirmed']">
                {{ item.status === 'forming' ? '组建中' : '已确认' }}
              </span>
            </td>
            <td>{{ formatTime(item.create_time) }}</td>
            <td class="ops">
              <button class="btn-sm" @click="showDetail(item)">详情</button>
              <button class="btn-sm danger" v-if="item.status === 'forming'" @click="dissolve(item)">解散</button>
            </td>
          </tr>
          <tr v-if="list.length === 0"><td colspan="8" class="empty">暂无数据</td></tr>
        </tbody>
      </table>
    </div>

    <div class="pagination" v-if="total > size">
      <button :disabled="page <= 1" @click="changePage(page - 1)">‹</button>
      <span class="page-info">{{ page }} / {{ totalPages }}</span>
      <button :disabled="page >= totalPages" @click="changePage(page + 1)">›</button>
    </div>

    <!-- 队伍详情弹窗 -->
    <div class="modal-overlay" v-if="detailTeam" @click.self="detailTeam = null">
      <div class="modal">
        <h3>队伍详情 #{{ detailTeam.team_id }}</h3>
        <div class="detail-info">
          <div class="detail-row"><span class="label">队名：</span>{{ detailTeam.team_name }}</div>
          <div class="detail-row"><span class="label">队长：</span>{{ detailTeam.captainName }}</div>
          <div class="detail-row"><span class="label">选题：</span>{{ detailTeam.apply_topic || '-' }}</div>
          <div class="detail-row"><span class="label">状态：</span>{{ detailTeam.status === 'forming' ? '组建中' : '已确认' }}</div>
          <div class="detail-row"><span class="label">成员列表：</span></div>
          <div class="member-list">
            <div v-for="m in members" :key="m.student_id" class="member-item">
              <span>{{ m.real_name }}</span>
              <span class="role">{{ m.role_in_team }}</span>
            </div>
            <div v-if="members.length === 0" class="empty">暂无成员</div>
          </div>
        </div>
        <div class="modal-actions">
          <button class="btn" @click="detailTeam = null">关闭</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { request } from '@/api/request'

const page = ref(1)
const size = ref(20)
const list = ref([])
const total = ref(0)
const detailTeam = ref(null)
const members = ref([])
const msg = ref({ text: '', type: 'success' })

function showMessage(text, type = 'success') {
  msg.value = { text, type }
  setTimeout(() => msg.value = { text: '', type: 'success' }, 3000)
}

const totalPages = computed(() => Math.max(1, Math.ceil(total.value / size.value)))

function formatTime(t) { return t ? t.substring(0, 19).replace('T', ' ') : '-' }

async function fetchList() {
  try {
    const res = await request.get('/admin/team/list', {
      params: { page: page.value, size: size.value }
    })
    if (res.data.code === 200) {
      list.value = res.data.data.list || []
      total.value = res.data.data.total || 0
    }
  } catch (e) { console.error(e) }
}

function changePage(p) { page.value = p; void fetchList() }

async function showDetail(item) {
  detailTeam.value = item
  try {
    const res = await request.get(`/admin/team/detail/${item.team_id}`)
    if (res.data.code === 200) {
      members.value = res.data.data.members || []
    }
  } catch (e) { members.value = [] }
}

async function dissolve(item) {
  if (!confirm(`确定解散"${item.team_name}"？此操作不可撤销。`)) return
  try {
    const res = await request.post(`/admin/team/dissolve/${item.team_id}`)
    if (res.data.code === 200) {
      detailTeam.value = null
      showMessage('队伍已解散')
      void fetchList()
    } else {
      showMessage(res.data.msg || '解散失败', 'error')
    }
  } catch (e) { showMessage('解散失败', 'error') }
}

onMounted(fetchList)
</script>

<style scoped>
.team-manage { max-width: 1200px; }
.toolbar { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px; }
.page-title { font-size: 18px; color: #333; margin: 0; }
.stat-bar { font-size: 13px; color: #888; margin-bottom: 12px; }
.table-wrap { background: #fff; border-radius: 10px; overflow: hidden; box-shadow: 0 1px 4px rgba(0,0,0,0.06); }
table { width: 100%; border-collapse: collapse; font-size: 13px; }
th { background: #fafafa; padding: 12px 14px; text-align: left; font-weight: 600; color: #555; border-bottom: 2px solid #eee; white-space: nowrap; }
td { padding: 11px 14px; border-bottom: 1px solid #f0f0f0; color: #333; }
tr:last-child td { border-bottom: none; }
tr:hover td { background: #f8f9ff; }
.empty { text-align: center; padding: 40px; color: #999; }
.badge { display: inline-block; padding: 2px 10px; border-radius: 10px; font-size: 12px; }
.badge.forming { background: #fff3e0; color: #e65100; }
.badge.confirmed { background: #e8f5e9; color: #2e7d32; }
.ops { white-space: nowrap; display: flex; gap: 6px; }
.btn-sm { padding: 4px 12px; border: 1px solid #ddd; border-radius: 4px; cursor: pointer; font-size: 12px; background: #fff; }
.btn-sm.danger { color: #c62828; border-color: #ef9a9a; }
.pagination { display: flex; justify-content: center; align-items: center; gap: 8px; margin-top: 16px; }
.pagination button { padding: 6px 14px; border: 1px solid #ddd; border-radius: 6px; background: #fff; cursor: pointer; font-size: 16px; }
.pagination button:disabled { opacity: 0.4; cursor: default; }
.page-info { font-size: 13px; color: #666; }
.modal-overlay { position: fixed; inset: 0; background: rgba(0,0,0,0.4); display: flex; align-items: center; justify-content: center; z-index: 100; }
.modal { background: #fff; border-radius: 12px; padding: 28px; width: 480px; max-width: 90vw; max-height: 80vh; overflow-y: auto; }
.modal h3 { font-size: 17px; margin-bottom: 18px; }
.detail-info { font-size: 14px; color: #333; }
.detail-row { margin-bottom: 10px; }
.detail-row .label { font-weight: 500; color: #666; display: inline-block; width: 80px; }
.member-list { background: #f9f9f9; border-radius: 8px; padding: 12px; margin-top: 4px; }
.member-item { display: flex; justify-content: space-between; padding: 6px 0; border-bottom: 1px solid #eee; }
.member-item:last-child { border-bottom: none; }
.role { color: #999; font-size: 12px; }
.modal-actions { display: flex; justify-content: flex-end; gap: 10px; margin-top: 20px; }
.btn { padding: 8px 20px; border: 1px solid #ddd; border-radius: 6px; cursor: pointer; font-size: 13px; background: #fff; }
.toast { padding: 10px 16px; border-radius: 8px; margin-bottom: 12px; font-size: 13px; }
.toast.success { background: #e8f5e9; color: #2e7d32; border: 1px solid #a5d6a7; }
.toast.error { background: #fbe9e7; color: #c62828; border: 1px solid #ef9a9a; }
</style>
