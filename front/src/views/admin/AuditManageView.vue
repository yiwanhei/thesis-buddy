<template>
  <div class="audit-manage">
    <div class="toolbar">
      <h2 class="page-title">✅ 审核管理</h2>
      <div class="filter-tabs">
        <button :class="['tab', { active: filterStatus === null }]" @click="filterStatus = null; page=1; fetchList()">全部</button>
        <button :class="['tab', { active: filterStatus === 1 }]" @click="filterStatus = 1; page=1; fetchList()">待审核</button>
        <button :class="['tab', { active: filterStatus === 2 }]" @click="filterStatus = 2; page=1; fetchList()">已通过</button>
        <button :class="['tab', { active: filterStatus === 4 }]" @click="filterStatus = 4; page=1; fetchList()">已驳回</button>
      </div>
    </div>
    <div class="stat-bar">共 <strong>{{ total }}</strong> 条申请记录</div>

    <!-- 消息提示 -->
    <div v-if="msg.text" :class="['toast', msg.type]">{{ msg.text }}</div>

    <div class="table-wrap">
      <table>
        <thead>
          <tr>
            <th>ID</th>
            <th>学生</th>
            <th>学号</th>
            <th>选题</th>
            <th>队伍ID</th>
            <th>类型</th>
            <th>状态</th>
            <th>申请时间</th>
            <th>操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="item in list" :key="item.application_id">
            <td>{{ item.application_id }}</td>
            <td>{{ item.studentName }}</td>
            <td>{{ item.studentAccount }}</td>
            <td>{{ item.topic_name || '-' }}</td>
            <td>{{ item.team_id || '-' }}</td>
            <td>{{ item.apply_type || '-' }}</td>
            <td>
              <span :class="['badge', statusClass(item.application_status)]">
                {{ statusText(item.application_status) }}
              </span>
            </td>
            <td>{{ formatTime(item.apply_time) }}</td>
            <td class="ops">
              <template v-if="item.application_status === 1">
                <button class="btn-sm ok" @click="review(item, 2)">通过</button>
                <button class="btn-sm warn" @click="review(item, 4)">驳回</button>
              </template>
              <span v-else class="done-text">{{ item.application_status === 2 ? '已通过' : '已驳回' }}</span>
            </td>
          </tr>
          <tr v-if="list.length === 0"><td colspan="9" class="empty">暂无数据</td></tr>
        </tbody>
      </table>
    </div>

    <div class="pagination" v-if="total > size">
      <button :disabled="page <= 1" @click="changePage(page - 1)">‹</button>
      <span class="page-info">{{ page }} / {{ totalPages }}</span>
      <button :disabled="page >= totalPages" @click="changePage(page + 1)">›</button>
    </div>

    <!-- 审核备注弹窗 -->
    <div class="modal-overlay" v-if="showRemarkDialog" @click.self="cancelReview">
      <div class="modal">
        <h3>{{ reviewAction === 2 ? '确认通过' : '驳回申请' }}</h3>
        <p class="hint">备注（可选）：</p>
        <textarea v-model="reviewRemark" rows="3" :placeholder="reviewAction === 4 ? '请填写驳回理由...' : '备注信息...'"></textarea>
        <div class="modal-actions">
          <button class="btn" @click="cancelReview">取消</button>
          <button class="btn" :class="reviewAction === 2 ? 'btn-primary' : 'btn-danger'" @click="confirmReview">{{ reviewAction === 2 ? '通过' : '驳回' }}</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { request } from '@/api/request'

const filterStatus = ref(null)
const page = ref(1)
const size = ref(20)
const list = ref([])
const total = ref(0)
const msg = ref({ text: '', type: 'success' })
const showRemarkDialog = ref(false)
const reviewItem = ref(null)
const reviewAction = ref(2)
const reviewRemark = ref('')

function showMessage(text, type = 'success') {
  msg.value = { text, type }
  setTimeout(() => msg.value = { text: '', type: 'success' }, 3000)
}

const totalPages = computed(() => Math.max(1, Math.ceil(total.value / size.value)))

function statusClass(s) {
  switch (s) {
    case 1: return 'pending'
    case 2: return 'approved'
    case 3: return 'confirmed'
    case 4: return 'rejected'
    default: return ''
  }
}

function statusText(s) {
  switch (s) {
    case 1: return '待审核'
    case 2: return '已通过'
    case 3: return '已确认'
    case 4: return '已驳回'
    default: return `状态${s}`
  }
}

function formatTime(t) { return t ? t.substring(0, 19).replace('T', ' ') : '-' }

async function fetchList() {
  try {
    const params = { page: page.value, size: size.value }
    if (filterStatus.value !== null) params.status = filterStatus.value
    const res = await request.get('/admin/audit/list', { params })
    if (res.data.code === 200) {
      list.value = res.data.data.list || []
      total.value = res.data.data.total || 0
    }
  } catch (e) { console.error(e) }
}

function changePage(p) { page.value = p; void fetchList() }

async function review(item, status) {
  reviewItem.value = item
  reviewAction.value = status
  reviewRemark.value = ''
  showRemarkDialog.value = true
}

async function confirmReview() {
  try {
    const params = { applicationId: reviewItem.value.application_id, applicationStatus: reviewAction.value }
    if (reviewRemark.value) params.remark = reviewRemark.value
    const res = await request.post('/admin/audit/review', null, { params })
    if (res.data.code === 200) {
      showRemarkDialog.value = false
      showMessage(res.data.msg || '审核完成')
      void fetchList()
    } else {
      showMessage(res.data.msg || '审核失败', 'error')
    }
  } catch (e) { showMessage('审核失败', 'error') }
}

function cancelReview() {
  showRemarkDialog.value = false
  reviewItem.value = null
}

onMounted(fetchList)
</script>

<style scoped>
.audit-manage { max-width: 1200px; }
.toolbar { display: flex; justify-content: space-between; align-items: center; flex-wrap: wrap; gap: 12px; margin-bottom: 16px; }
.page-title { font-size: 18px; color: #333; margin: 0; }
.filter-tabs { display: flex; gap: 0; }
.tab { padding: 8px 18px; border: 1px solid #ddd; background: #f9f9f9; cursor: pointer; font-size: 13px; }
.tab:first-child { border-radius: 6px 0 0 6px; }
.tab:last-child { border-radius: 0 6px 6px 0; }
.tab.active { background: #1a1a2e; color: #fff; border-color: #1a1a2e; }
.stat-bar { font-size: 13px; color: #888; margin-bottom: 12px; }
.table-wrap { background: #fff; border-radius: 10px; overflow: hidden; box-shadow: 0 1px 4px rgba(0,0,0,0.06); }
table { width: 100%; border-collapse: collapse; font-size: 13px; }
th { background: #fafafa; padding: 12px 14px; text-align: left; font-weight: 600; color: #555; border-bottom: 2px solid #eee; white-space: nowrap; }
td { padding: 11px 14px; border-bottom: 1px solid #f0f0f0; color: #333; }
tr:last-child td { border-bottom: none; }
tr:hover td { background: #f8f9ff; }
.empty { text-align: center; padding: 40px; color: #999; }
.badge { display: inline-block; padding: 2px 10px; border-radius: 10px; font-size: 12px; }
.badge.pending { background: #fff3e0; color: #e65100; }
.badge.approved { background: #e3f2fd; color: #1565c0; }
.badge.confirmed { background: #e8f5e9; color: #2e7d32; }
.badge.rejected { background: #fbe9e7; color: #c62828; }
.ops { white-space: nowrap; display: flex; gap: 6px; align-items: center; }
.btn-sm { padding: 4px 12px; border: 1px solid #ddd; border-radius: 4px; cursor: pointer; font-size: 12px; background: #fff; }
.btn-sm.ok { color: #2e7d32; border-color: #a5d6a7; }
.btn-sm.warn { color: #c62828; border-color: #ef9a9a; }
.done-text { font-size: 12px; color: #999; }
.pagination { display: flex; justify-content: center; align-items: center; gap: 8px; margin-top: 16px; }
.pagination button { padding: 6px 14px; border: 1px solid #ddd; border-radius: 6px; background: #fff; cursor: pointer; font-size: 16px; }
.pagination button:disabled { opacity: 0.4; cursor: default; }
.page-info { font-size: 13px; color: #666; }
.toast { padding: 10px 16px; border-radius: 8px; margin-bottom: 12px; font-size: 13px; }
.toast.success { background: #e8f5e9; color: #2e7d32; border: 1px solid #a5d6a7; }
.toast.error { background: #fbe9e7; color: #c62828; border: 1px solid #ef9a9a; }
.modal-overlay { position: fixed; inset: 0; background: rgba(0,0,0,0.4); display: flex; align-items: center; justify-content: center; z-index: 100; }
.modal { background: #fff; border-radius: 12px; padding: 28px; width: 420px; max-width: 90vw; }
.modal h3 { font-size: 17px; margin-bottom: 18px; }
.modal-actions { display: flex; justify-content: flex-end; gap: 10px; margin-top: 20px; }
.hint { font-size: 12px; color: #888; margin-bottom: 8px; }
textarea { width: 100%; border: 1px solid #ddd; border-radius: 6px; padding: 10px; font-size: 13px; font-family: inherit; resize: vertical; box-sizing: border-box; outline: none; }
textarea:focus { border-color: #302b63; }
.btn { padding: 8px 20px; border: 1px solid #ddd; border-radius: 6px; cursor: pointer; font-size: 13px; background: #fff; }
.btn-primary { background: #1a1a2e; color: #fff; border-color: #1a1a2e; }
.btn-danger { background: #c62828; color: #fff; border-color: #c62828; }
</style>
