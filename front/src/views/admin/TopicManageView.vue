<template>
  <div class="topic-manage">
    <div class="toolbar">
      <div class="search-box">
        <input v-model="keyword" placeholder="搜索选题名称或类别..." @keyup.enter="search" />
        <button class="btn-search" @click="search">🔍</button>
      </div>
      <button class="btn btn-primary" @click="openAdd">+ 新增选题</button>
    </div>

    <div class="stat-bar">共 <strong>{{ total }}</strong> 个选题</div>
    
    <!-- 消息提示 -->
    <div v-if="msg.text" :class="['toast', msg.type]">{{ msg.text }}</div>

    <div class="table-wrap">
      <table>
        <thead>
          <tr>
            <th>ID</th>
            <th>选题名称</th>
            <th>类别</th>
            <th>成果形式</th>
            <th>最大人数</th>
            <th>状态</th>
            <th>创建时间</th>
            <th>操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="item in list" :key="item.topicId">
            <td>{{ item.topicId }}</td>
            <td>{{ item.topicName }}</td>
            <td>{{ item.category }}</td>
            <td>{{ item.resultForm || '-' }}</td>
            <td>{{ item.maxCapacity }}</td>
            <td>
              <span :class="['badge', item.status === 'open' ? 'active' : 'disabled']">
                {{ item.status === 'open' ? '开放' : '关闭' }}
              </span>
            </td>
            <td>{{ formatTime(item.createTime) }}</td>
            <td class="ops">
              <button class="btn-sm" @click="openEdit(item)">编辑</button>
              <button class="btn-sm" :class="item.status === 'open' ? 'warn' : 'ok'" @click="toggleStatus(item)">
                {{ item.status === 'open' ? '关闭' : '开放' }}
              </button>
              <button class="btn-sm danger" @click="deleteTopic(item)">删除</button>
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

    <!-- 新增/编辑弹窗 -->
    <div class="modal-overlay" v-if="showDialog" @click.self="showDialog = false">
      <div class="modal">
        <h3>{{ editing ? '编辑选题' : '新增选题' }}</h3>
        <div class="form">
          <div class="field">
            <label>选题名称</label>
            <input v-model="form.topicName" placeholder="请输入选题名称" />
          </div>
          <div class="field">
            <label>类别</label>
            <input v-model="form.category" placeholder="如：软件开发、人工智能" />
          </div>
          <div class="field">
            <label>成果形式</label>
            <input v-model="form.resultForm" placeholder="如：论文、系统、报告" />
          </div>
          <div class="field">
            <label>最大人数</label>
            <input v-model="form.maxCapacity" type="number" min="1" />
          </div>
          <div class="field">
            <label>状态</label>
            <select v-model="form.status">
              <option value="open">开放</option>
              <option value="closed">关闭</option>
            </select>
          </div>
        </div>
        <div class="modal-actions">
          <button class="btn" @click="showDialog = false">取消</button>
          <button class="btn btn-primary" @click="save">{{ editing ? '保存' : '添加' }}</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { request } from '@/api/request'

const keyword = ref('')
const page = ref(1)
const size = ref(20)
const list = ref([])
const total = ref(0)
const showDialog = ref(false)
const editing = ref(false)
const form = ref({ topicName: '', category: '', resultForm: '', maxCapacity: 1, status: 'open' })
const msg = ref({ text: '', type: 'success' })

function showMessage(text, type = 'success') {
  msg.value = { text, type }
  setTimeout(() => msg.value = { text: '', type: 'success' }, 3000)
}

const totalPages = computed(() => Math.max(1, Math.ceil(total.value / size.value)))

function formatTime(t) { return t ? t.substring(0, 19).replace('T', ' ') : '-' }

async function fetchList() {
  try {
    const res = await request.get('/admin/topic/list', {
      params: { category: keyword.value || undefined, page: page.value, size: size.value }
    })
    if (res.data.code === 200) {
      list.value = res.data.data.list || []
      total.value = res.data.data.total || 0
    }
  } catch (e) { console.error(e) }
}

function search() { page.value = 1; fetchList() }
function changePage(p) { page.value = p; fetchList() }

function openAdd() {
  editing.value = false
  form.value = { topicName: '', category: '', resultForm: '', maxCapacity: 1, status: 'open' }
  showDialog.value = true
}

function openEdit(item) {
  editing.value = true
  form.value = { ...item }
  showDialog.value = true
}

async function save() {
  try {
    const url = editing.value ? '/admin/topic/update' : '/admin/topic/add'
    const res = await request({
      method: editing.value ? 'put' : 'post',
      url,
      data: form.value
    })
    if (res.data.code === 200) {
      showDialog.value = false
      showMessage(editing.value ? '更新成功' : '添加成功')
      fetchList()
    } else {
      showMessage(res.data.msg || '操作失败', 'error')
    }
  } catch (e) { showMessage('操作失败', 'error') }
}

async function toggleStatus(item) {
  const newStatus = item.status === 'open' ? 'closed' : 'open'
  try {
    const res = await request.put('/admin/topic/status', null, {
      params: { topicId: item.topicId, status: newStatus }
    })
    if (res.data.code === 200) {
      item.status = newStatus
      showMessage('状态已切换')
    }
  } catch (e) { showMessage('操作失败', 'error') }
}

async function deleteTopic(item) {
  if (!confirm(`确定删除"${item.topicName}"？`)) return
  try {
    const res = await request.delete(`/admin/topic/${item.topicId}`)
    if (res.data.code === 200) {
      showMessage('已删除')
      fetchList()
    } else {
      showMessage(res.data.msg || '删除失败', 'error')
    }
  } catch (e) { showMessage('删除失败', 'error') }
}

onMounted(fetchList)
</script>

<style scoped>
.topic-manage { max-width: 1200px; }
.toolbar { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px; }
.search-box { display: flex; border: 1px solid #ddd; border-radius: 6px; overflow: hidden; }
.search-box input { border: none; padding: 8px 12px; outline: none; width: 200px; font-size: 13px; }
.btn-search { border: none; background: #f5f5f5; padding: 8px 10px; cursor: pointer; }
.stat-bar { font-size: 13px; color: #888; margin-bottom: 12px; }
.table-wrap { background: #fff; border-radius: 10px; overflow: hidden; box-shadow: 0 1px 4px rgba(0,0,0,0.06); }
table { width: 100%; border-collapse: collapse; font-size: 13px; }
th { background: #fafafa; padding: 12px 14px; text-align: left; font-weight: 600; color: #555; border-bottom: 2px solid #eee; white-space: nowrap; }
td { padding: 11px 14px; border-bottom: 1px solid #f0f0f0; color: #333; }
tr:last-child td { border-bottom: none; }
tr:hover td { background: #f8f9ff; }
.empty { text-align: center; padding: 40px; color: #999; }
.badge { display: inline-block; padding: 2px 10px; border-radius: 10px; font-size: 12px; }
.badge.active { background: #e8f5e9; color: #2e7d32; }
.badge.disabled { background: #fbe9e7; color: #c62828; }
.ops { white-space: nowrap; display: flex; gap: 6px; }
.btn-sm { padding: 4px 12px; border: 1px solid #ddd; border-radius: 4px; cursor: pointer; font-size: 12px; background: #fff; }
.btn-sm.ok { color: #2e7d32; border-color: #a5d6a7; }
.btn-sm.warn { color: #c62828; border-color: #ef9a9a; }
.btn-sm.danger { color: #c62828; border-color: #ef9a9a; }
.pagination { display: flex; justify-content: center; align-items: center; gap: 8px; margin-top: 16px; }
.pagination button { padding: 6px 14px; border: 1px solid #ddd; border-radius: 6px; background: #fff; cursor: pointer; font-size: 16px; }
.pagination button:disabled { opacity: 0.4; cursor: default; }
.page-info { font-size: 13px; color: #666; }
.modal-overlay { position: fixed; inset: 0; background: rgba(0,0,0,0.4); display: flex; align-items: center; justify-content: center; z-index: 100; }
.modal { background: #fff; border-radius: 12px; padding: 28px; width: 460px; max-width: 90vw; }
.modal h3 { font-size: 17px; margin-bottom: 18px; }
.form { display: flex; flex-direction: column; gap: 12px; }
.field { display: flex; flex-direction: column; gap: 4px; }
.field label { font-size: 13px; color: #555; font-weight: 500; }
.field input, .field select { padding: 9px 12px; border: 1px solid #ddd; border-radius: 6px; font-size: 14px; outline: none; }
.field input:focus, .field select:focus { border-color: #302b63; }
.modal-actions { display: flex; justify-content: flex-end; gap: 10px; margin-top: 20px; }
.btn { padding: 8px 20px; border: 1px solid #ddd; border-radius: 6px; cursor: pointer; font-size: 13px; background: #fff; }
.btn-primary { background: #1a1a2e; color: #fff; border-color: #1a1a2e; }
.toast { padding: 10px 16px; border-radius: 8px; margin-bottom: 12px; font-size: 13px; }
.toast.success { background: #e8f5e9; color: #2e7d32; border: 1px solid #a5d6a7; }
.toast.error { background: #fbe9e7; color: #c62828; border: 1px solid #ef9a9a; }
</style>
