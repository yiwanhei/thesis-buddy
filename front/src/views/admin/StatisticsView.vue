<template>
  <div class="statistics-page">
    <h2 class="page-title">📈 数据统计</h2>

    <!-- 概览卡片 -->
    <div class="stat-cards">
      <div class="stat-card">
        <div class="stat-label">学生总数</div>
        <div class="stat-value student">{{ dashboard.studentCount || 0 }}</div>
      </div>
      <div class="stat-card">
        <div class="stat-label">教师总数</div>
        <div class="stat-value teacher">{{ dashboard.teacherCount || 0 }}</div>
      </div>
      <div class="stat-card">
        <div class="stat-label">选题总数</div>
        <div class="stat-value topic">{{ dashboard.topicCount || 0 }}</div>
      </div>
      <div class="stat-card">
        <div class="stat-label">队伍总数</div>
        <div class="stat-value team">{{ dashboard.teamCount || 0 }}</div>
      </div>
      <div class="stat-card">
        <div class="stat-label">待审核</div>
        <div class="stat-value pending">{{ dashboard.pendingCount || 0 }}</div>
      </div>
      <div class="stat-card">
        <div class="stat-label">已确认</div>
        <div class="stat-value confirmed">{{ dashboard.confirmedCount || 0 }}</div>
      </div>
    </div>

    <!-- ECharts 图表 -->
    <div class="charts-row">
      <div class="chart-card">
        <h3>选题类别分布</h3>
        <v-chart :option="categoryOption" style="height:260px" autoresize />
      </div>
      <div class="chart-card">
        <h3>各模块数据对比</h3>
        <v-chart :option="barOption" style="height:260px" autoresize />
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { use } from 'echarts/core'
import { CanvasRenderer } from 'echarts/renderers'
import { PieChart, BarChart } from 'echarts/charts'
import { TitleComponent, TooltipComponent, LegendComponent, GridComponent } from 'echarts/components'
import VChart from 'vue-echarts'
import { request } from '@/api/request'

use([CanvasRenderer, PieChart, BarChart, TooltipComponent, LegendComponent, GridComponent])

const dashboard = ref({
  studentCount: 0, teacherCount: 0, topicCount: 0,
  teamCount: 0, pendingCount: 0, confirmedCount: 0
})

onMounted(async () => {
  try {
    const res = await request.get('/admin/user/dashboard')
    if (res.data.code === 200) {
      dashboard.value = res.data.data || dashboard.value
    }
  } catch (e) { /* 使用默认值 */ }
})

const categoryOption = computed(() => ({
  tooltip: { trigger: 'item' },
  legend: { bottom: 0, textStyle: { fontSize: 12 } },
  series: [{
    type: 'pie', radius: ['40%', '70%'], center: ['50%', '45%'],
    avoidLabelOverlap: false, label: { show: false },
    emphasis: { label: { show: true, fontSize: 14, fontWeight: 'bold' } },
    itemStyle: { borderRadius: 4, borderColor: '#fff', borderWidth: 2 },
    data: [
      { value: 45, name: '软件开发', itemStyle: { color: '#e94560' } },
      { value: 32, name: '人工智能', itemStyle: { color: '#1890ff' } },
      { value: 28, name: '数据分析', itemStyle: { color: '#722ed1' } },
      { value: 21, name: '网络安全', itemStyle: { color: '#fa8c16' } },
      { value: 16, name: '嵌入式系统', itemStyle: { color: '#52c41a' } }
    ]
  }]
}))

const barOption = computed(() => ({
  tooltip: { trigger: 'axis' },
  grid: { left: 50, right: 30, bottom: 30, top: 20 },
  xAxis: { type: 'category', data: ['学生', '教师', '选题', '队伍', '待审核', '已确认'], axisLabel: { fontSize: 12 } },
  yAxis: { type: 'value' },
  series: [{
    type: 'bar', barWidth: '45%',
    itemStyle: {
      borderRadius: [4, 4, 0, 0],
      color: { type: 'linear', x: 0, y: 0, x2: 0, y2: 1, colorStops: [
        { offset: 0, color: '#667eea' }, { offset: 1, color: '#764ba2' }
      ]}
    },
    data: [
      dashboard.value.studentCount,
      dashboard.value.teacherCount,
      dashboard.value.topicCount,
      dashboard.value.teamCount,
      dashboard.value.pendingCount,
      dashboard.value.confirmedCount
    ]
  }]
}))
</script>

<style scoped>
.statistics-page { max-width: 1000px; }
.page-title { font-size: 18px; margin-bottom: 20px; color: #333; }
.stat-cards { display: grid; grid-template-columns: repeat(3, 1fr); gap: 16px; margin-bottom: 24px; }
.stat-card { background: #fff; padding: 20px; border-radius: 10px; box-shadow: 0 1px 4px rgba(0,0,0,0.06); }
.stat-label { font-size: 13px; color: #888; margin-bottom: 8px; }
.stat-value { font-size: 28px; font-weight: 700; }
.stat-value.student { color: #e94560; }
.stat-value.teacher { color: #1890ff; }
.stat-value.topic { color: #722ed1; }
.stat-value.team { color: #fa8c16; }
.stat-value.pending { color: #ff4d4f; }
.stat-value.confirmed { color: #52c41a; }
.charts-row { display: grid; grid-template-columns: 1fr 1fr; gap: 16px; }
.chart-card { background: #fff; padding: 20px; border-radius: 10px; box-shadow: 0 1px 4px rgba(0,0,0,0.06); }
.chart-card h3 { font-size: 14px; color: #333; margin-bottom: 12px; font-weight: 600; }
</style>
