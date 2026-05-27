<template>
  <div class="user-manage">
    <!-- 操作栏 -->
    <div class="toolbar">
      <div class="tabs">
        <button :class="['tab', { active: userType === 'student' }]" @click="switchTab('student')">学生管理</button>
        <button :class="['tab', { active: userType === 'teacher' }]" @click="switchTab('teacher')">教师管理</button>
      </div>
      <div class="actions">
        <div class="search-box">
          <input v-model="keyword" placeholder="搜索账号/姓名..." @keyup.enter="search" />
          <button class="btn-search" @click="search">🔍</button>
        </div>
        <button class="btn btn-primary" @click="showAddDialog = true">+ 新增</button>
        <button class="btn btn-secondary" @click="showImportDialog = true">📥 批量导入</button>
      </div>
    </div>
    
    <!-- 消息提示 -->
    <div v-if="msg.text" :class="['toast', msg.type]">{{ msg.text }}</div>

    <!-- 统计信息 -->
    <div class="stat-bar">
      <span>共 <strong>{{ total }}</strong> 条记录</span>
    </div>

    <!-- 数据表格 -->
    <div class="table-wrap">
      <table>
        <thead>
          <tr>
            <th>ID</th>
            <th>账号</th>
            <th>姓名</th>
            <th v-if="userType === 'student'">性别</th>
            <th v-if="userType === 'student'">班级</th>
            <th v-if="userType === 'teacher'">职称</th>
            <th>状态</th>
            <th>创建时间</th>
            <th>操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="item in list" :key="item.studentId || item.teacherId">
            <td>{{ item.studentId || item.teacherId }}</td>
            <td>{{ item.account }}</td>
            <td>{{ item.realName }}</td>
            <td v-if="userType === 'student'">{{ item.gender || '-' }}</td>
            <td v-if="userType === 'student'">{{ getClassName(item.classId) }}</td>
            <td v-if="userType === 'teacher'">{{ item.title || '-' }}</td>
            <td>
              <span :class="['badge', item.status === 1 ? 'active' : 'disabled']">
                {{ item.status === 1 ? '正常' : '禁用' }}
              </span>
            </td>
            <td>{{ formatTime(item.createTime) }}</td>
            <td class="ops">
              <button class="btn-sm" :class="item.status === 1 ? 'warn' : 'ok'" @click="toggleStatus(item)">
                {{ item.status === 1 ? '禁用' : '启用' }}
              </button>
            </td>
          </tr>
          <tr v-if="list.length === 0"><td :colspan="userType === 'student' ? 9 : 8" class="empty">暂无数据</td></tr>
        </tbody>
      </table>
    </div>

    <!-- 分页 -->
    <div class="pagination" v-if="total > size">
      <button :disabled="page <= 1" @click="changePage(page - 1)">‹</button>
      <span class="page-info">{{ page }} / {{ totalPages }}</span>
      <button :disabled="page >= totalPages" @click="changePage(page + 1)">›</button>
    </div>

    <!-- 新增用户弹窗 -->
    <div class="modal-overlay" v-if="showAddDialog" @click.self="showAddDialog = false">
      <div class="modal">
        <h3>{{ userType === 'student' ? '新增学生' : '新增教师' }}</h3>
        <div class="form">
          <div class="field">
            <label>账号</label>
            <input v-model="addForm.account" placeholder="学号/工号" />
          </div>
          <div class="field">
            <label>姓名</label>
            <input v-model="addForm.realName" placeholder="真实姓名" />
          </div>
          <div class="field" v-if="userType === 'student'">
            <label>性别</label>
            <select v-model="addForm.gender">
              <option value="男">男</option>
              <option value="女">女</option>
            </select>
          </div>
          <div class="field" v-if="userType === 'student'">
            <label>班级</label>
            <select v-model="addForm.classId">
              <option v-for="c in classList" :key="c.classId" :value="c.classId">{{ c.className }}</option>
            </select>
          </div>
          <div class="field" v-if="userType === 'teacher'">
            <label>职称</label>
            <input v-model="addForm.title" placeholder="如：教授、副教授" />
          </div>
          <div class="field" v-if="userType === 'teacher'">
            <label>所属校区</label>
            <input v-model="addForm.campusId" type="number" placeholder="校区ID" />
          </div>
        </div>
        <div class="modal-actions">
          <button class="btn" @click="showAddDialog = false">取消</button>
          <button class="btn btn-primary" @click="addUser">确定添加</button>
        </div>
      </div>
    </div>

    <!-- 批量导入弹窗 -->
    <div class="modal-overlay" v-if="showImportDialog" @click.self="showImportDialog = false">
      <div class="modal">
        <h3>批量导入学生</h3>
        <p class="hint">每行输入：学号,姓名,性别,班级ID（如：2024001,张三,男,1）</p>
        <textarea v-model="importText" rows="8" placeholder="2024001,张三,男,1&#10;2024002,李四,女,1"></textarea>
        <div class="modal-actions">
          <button class="btn" @click="showImportDialog = false">取消</button>
          <button class="btn btn-primary" @click="batchImport" :disabled="!importText.trim()">导入</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { request } from '@/api/request'

const userType = ref('student')
const keyword = ref('')
const page = ref(1)
const size = ref(20)
const list = ref([])
const total = ref(0)
const classList = ref([])
const showAddDialog = ref(false)
const showImportDialog = ref(false)
const importText = ref('')
const addForm = ref({ account: '', realName: '', gender: '男', classId: null, title: '', campusId: null })
const msg = ref({ text: '', type: 'success' })

function showMessage(text, type = 'success') {
  msg.value = { text, type }
  setTimeout(() => msg.value = { text: '', type: 'success' }, 3000)
}

const totalPages = computed(() => Math.max(1, Math.ceil(total.value / size.value)))

function getClassName(classId) {
  const c = classList.value.find(c => c.classId === classId)
  return c ? c.className : `班级${classId}`
}

function formatTime(t) {
  return t ? t.substring(0, 19).replace('T', ' ') : '-'
}

async function fetchList() {
  try {
    const res = await request.get('/admin/user/list', {
      params: { type: userType.value, keyword: keyword.value, page: page.value, size: size.value }
    })
    if (res.data.code === 200) {
      list.value = res.data.data.list || []
      total.value = res.data.data.total || 0
    }
  } catch (e) { console.error(e) }
}

async function fetchClasses() {
  try {
    const res = await request.get('/admin/user/classes')
    if (res.data.code === 200) classList.value = res.data.data || []
  } catch (e) { console.error(e) }
}

function switchTab(tab) {
  userType.value = tab
  page.value = 1
  keyword.value = ''
  fetchList()
}

function search() {
  page.value = 1
  fetchList()
}

function changePage(p) {
  page.value = p
  fetchList()
}

async function toggleStatus(item) {
  const id = item.studentId || item.teacherId
  const newStatus = item.status === 1 ? 0 : 1
  try {
    const res = await request.put('/admin/user/status', {
      userId: id,
      userType: userType.value,
      status: newStatus
    })
    if (res.data.code === 200) {
      item.status = newStatus
      showMessage('状态修改成功')
    } else {
      showMessage(res.data.msg || '操作失败', 'error')
    }
  } catch (e) { showMessage('网络错误', 'error') }
}

async function addUser() {
  const data = { ...addForm.value, userType: userType.value, password: '123456' }
  try {
    const res = await request.post('/admin/user/add', data)
    if (res.data.code === 200) {
      showAddDialog.value = false
      addForm.value = { account: '', realName: '', gender: '男', classId: null, title: '', campusId: null }
      showMessage('添加成功')
      fetchList()
    } else {
      showMessage(res.data.msg || '添加失败', 'error')
    }
  } catch (e) { showMessage('网络错误', 'error') }
}

async function batchImport() {
  const lines = importText.value.trim().split('\n').filter(l => l.trim())
  const students = lines.map(l => {
    const parts = l.split(',').map(s => s.trim())
    return { account: parts[0], realName: parts[1], gender: parts[2] || '男', classId: parseInt(parts[3]) || 1 }
  })
  try {
    const res = await request.post('/admin/user/batch-import', { students })
    if (res.data.code === 200) {
      showImportDialog.value = false
      importText.value = ''
      showMessage('导入成功')
      fetchList()
    } else {
      showMessage(res.data.msg || '导入失败', 'error')
    }
  } catch (e) { showMessage('网络错误', 'error') }
}

onMounted(() => {
  fetchList()
  fetchClasses()
})
</script>

<style scoped>
.user-manage { max-width: 1200px; }
.toolbar { display: flex; justify-content: space-between; align-items: center; flex-wrap: wrap; gap: 12px; margin-bottom: 16px; }
.tabs { display: flex; gap: 0; }
.tab { padding: 8px 20px; border: 1px solid #ddd; background: #f9f9f9; cursor: pointer; font-size: 14px; }
.tab:first-child { border-radius: 6px 0 0 6px; }
.tab:last-child { border-radius: 0 6px 6px 0; }
.tab.active { background: #1a1a2e; color: #fff; border-color: #1a1a2e; }
.actions { display: flex; gap: 8px; align-items: center; }
.search-box { display: flex; border: 1px solid #ddd; border-radius: 6px; overflow: hidden; }
.search-box input { border: none; padding: 8px 12px; outline: none; width: 180px; font-size: 13px; }
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
.ops { white-space: nowrap; }
.btn-sm { padding: 4px 12px; border: 1px solid #ddd; border-radius: 4px; cursor: pointer; font-size: 12px; background: #fff; }
.btn-sm.ok { color: #2e7d32; border-color: #a5d6a7; }
.btn-sm.warn { color: #c62828; border-color: #ef9a9a; }
.pagination { display: flex; justify-content: center; align-items: center; gap: 8px; margin-top: 16px; }
.pagination button { padding: 6px 14px; border: 1px solid #ddd; border-radius: 6px; background: #fff; cursor: pointer; font-size: 16px; }
.pagination button:disabled { opacity: 0.4; cursor: default; }
.page-info { font-size: 13px; color: #666; }
.modal-overlay { position: fixed; inset: 0; background: rgba(0,0,0,0.4); display: flex; align-items: center; justify-content: center; z-index: 100; }
.modal { background: #fff; border-radius: 12px; padding: 28px; width: 460px; max-width: 90vw; max-height: 80vh; overflow-y: auto; }
.modal h3 { font-size: 17px; margin-bottom: 18px; }
.form { display: flex; flex-direction: column; gap: 12px; }
.field { display: flex; flex-direction: column; gap: 4px; }
.field label { font-size: 13px; color: #555; font-weight: 500; }
.field input, .field select { padding: 9px 12px; border: 1px solid #ddd; border-radius: 6px; font-size: 14px; outline: none; }
.field input:focus, .field select:focus { border-color: #302b63; }
.modal-actions { display: flex; justify-content: flex-end; gap: 10px; margin-top: 20px; }
.btn { padding: 8px 20px; border: 1px solid #ddd; border-radius: 6px; cursor: pointer; font-size: 13px; background: #fff; }
.btn-primary { background: #1a1a2e; color: #fff; border-color: #1a1a2e; }
.btn-secondary { background: #fff; border-color: #1a1a2e; color: #1a1a2e; }
.hint { font-size: 12px; color: #888; margin-bottom: 8px; }
textarea { width: 100%; border: 1px solid #ddd; border-radius: 6px; padding: 10px; font-size: 13px; font-family: monospace; resize: vertical; box-sizing: border-box; outline: none; }
textarea:focus { border-color: #302b63; }
.toast { padding: 10px 16px; border-radius: 8px; margin-bottom: 12px; font-size: 13px; }
.toast.success { background: #e8f5e9; color: #2e7d32; border: 1px solid #a5d6a7; }
.toast.error { background: #fbe9e7; color: #c62828; border: 1px solid #ef9a9a; }
</style>
