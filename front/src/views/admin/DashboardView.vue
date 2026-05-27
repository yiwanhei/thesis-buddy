<template>
  <div class="dashboard">
    <!-- 核心指标卡片 -->
    <div class="stat-cards">
      <div class="stat-card">
        <div class="stat-label">学生总数</div>
        <div class="stat-value student">{{ stats.studentCount || 0 }}</div>
      </div>
      <div class="stat-card">
        <div class="stat-label">教师总数</div>
        <div class="stat-value teacher">{{ stats.teacherCount || 0 }}</div>
      </div>
      <div class="stat-card">
        <div class="stat-label">选题总数</div>
        <div class="stat-value topic">{{ stats.topicCount || 0 }}</div>
      </div>
      <div class="stat-card">
        <div class="stat-label">队伍总数</div>
        <div class="stat-value team">{{ stats.teamCount || 0 }}</div>
      </div>
      <div class="stat-card">
        <div class="stat-label">待审核申请</div>
        <div class="stat-value pending">{{ stats.pendingCount || 0 }}</div>
      </div>
      <div class="stat-card">
        <div class="stat-label">已确认选题</div>
        <div class="stat-value confirmed">{{ stats.confirmedCount || 0 }}</div>
      </div>
    </div>

    <!-- ECharts 图表区 -->
    <div class="charts-row">
      <div class="chart-card">
        <h3>选题类别分布</h3>
        <v-chart :option="categoryChartOption" style="height:280px" autoresize />
      </div>
      <div class="chart-card">
        <h3>申请趋势（近7天）</h3>
        <v-chart :option="trendChartOption" style="height:280px" autoresize />
      </div>
    </div>
    <div class="chart-card full-width">
      <h3>各模块数据概览</h3>
      <v-chart :option="overviewChartOption" style="height:280px" autoresize />
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { use } from 'echarts/core'
import { CanvasRenderer } from 'echarts/renderers'
import { PieChart, LineChart, BarChart } from 'echarts/charts'
import { TitleComponent, TooltipComponent, LegendComponent, GridComponent } from 'echarts/components'
import VChart from 'vue-echarts'
import { request } from '@/api/request'

use([CanvasRenderer, PieChart, LineChart, BarChart, TitleComponent, TooltipComponent, LegendComponent, GridComponent])

const stats = ref({
  studentCount: 0,
  teacherCount: 0,
  topicCount: 0,
  teamCount: 0,
  pendingCount: 0,
  confirmedCount: 0
})

onMounted(async () => {
  try {
    // 从仪表盘API获取统计数据
    const res = await request.get('/admin/user/dashboard')
    if (res.data.code === 200) {
      const d = res.data.data
      if (d) {
        stats.value.studentCount = d.studentCount || 0
        stats.value.teacherCount = d.teacherCount || 0
        stats.value.topicCount = d.topicCount || 0
        stats.value.teamCount = d.teamCount || 0
        stats.value.pendingCount = d.pendingCount || 0
        stats.value.confirmedCount = d.confirmedCount || 0
      }
    }
  } catch (e) { /* 仪表盘数据加载失败，使用默认值 */ }
})

// 饼图 - 示例数据
const categoryChartOption = computed(() => ({
  tooltip: { trigger: 'item' },
  legend: { bottom: 0, textStyle: { fontSize: 12 } },
  series: [{
    type: 'pie',
    radius: ['40%', '70%'],
    center: ['50%', '45%'],
    avoidLabelOverlap: false,
    label: { show: false },
    emphasis: {
      label: { show: true, fontSize: 14, fontWeight: 'bold' }
    },
    itemStyle: {
      borderRadius: 4,
      borderColor: '#fff',
      borderWidth: 2
    },
    data: [
      { value: 45, name: '软件开发', itemStyle: { color: '#e94560' } },
      { value: 32, name: '人工智能', itemStyle: { color: '#1890ff' } },
      { value: 28, name: '数据分析', itemStyle: { color: '#722ed1' } },
      { value: 21, name: '网络安全', itemStyle: { color: '#fa8c16' } },
      { value: 16, name: '嵌入式系统', itemStyle: { color: '#52c41a' } }
    ]
  }]
}))

// 折线图 - 近7天申请趋势
const trendChartOption = computed(() => ({
  tooltip: { trigger: 'axis' },
  grid: { left: 40, right: 20, bottom: 30, top: 20 },
  xAxis: {
    type: 'category',
    data: ['周一', '周二', '周三', '周四', '周五', '周六', '周日'],
    axisLabel: { fontSize: 11 }
  },
  yAxis: { type: 'value' },
  series: [{
    data: [15, 22, 18, 30, 25, 12, 8],
    type: 'line',
    smooth: true,
    lineStyle: { color: '#e94560', width: 3 },
    areaStyle: {
      color: {
        type: 'linear',
        x: 0, y: 0, x2: 0, y2: 1,
        colorStops: [
          { offset: 0, color: 'rgba(233,69,96,0.3)' },
          { offset: 1, color: 'rgba(233,69,96,0.02)' }
        ]
      }
    },
    symbol: 'circle',
    symbolSize: 8,
    itemStyle: { color: '#e94560' }
  }]
}))

// 柱状图 - 各模块数据概览
const overviewChartOption = computed(() => ({
  tooltip: { trigger: 'axis' },
  grid: { left: 50, right: 30, bottom: 30, top: 20 },
  xAxis: {
    type: 'category',
    data: ['学生', '教师', '选题', '队伍', '待审核', '已确认'],
    axisLabel: { fontSize: 12 }
  },
  yAxis: { type: 'value' },
  series: [{
    type: 'bar',
    barWidth: '45%',
    itemStyle: {
      borderRadius: [4, 4, 0, 0],
      color: {
        type: 'linear',
        x: 0, y: 0, x2: 0, y2: 1,
        colorStops: [
          { offset: 0, color: '#667eea' },
          { offset: 1, color: '#764ba2' }
        ]
      }
    },
    data: [
      stats.value.studentCount,
      stats.value.teacherCount,
      stats.value.topicCount,
      stats.value.teamCount,
      stats.value.pendingCount,
      stats.value.confirmedCount
    ]
  }]
}))
</script>

<style scoped>
.dashboard { max-width: 1200px; }

.stat-cards {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 16px;
  margin-bottom: 24px;
}

.stat-card {
  background: #fff;
  padding: 20px;
  border-radius: 10px;
  box-shadow: 0 1px 4px rgba(0,0,0,0.06);
}

.stat-label { font-size: 13px; color: #888; margin-bottom: 8px; }
.stat-value { font-size: 32px; font-weight: 700; }
.stat-value.student { color: #e94560; }
.stat-value.teacher { color: #1890ff; }
.stat-value.topic { color: #722ed1; }
.stat-value.team { color: #fa8c16; }
.stat-value.pending { color: #ff4d4f; }
.stat-value.confirmed { color: #52c41a; }

.charts-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;
  margin-bottom: 16px;
}

.chart-card {
  background: #fff;
  padding: 20px;
  border-radius: 10px;
  box-shadow: 0 1px 4px rgba(0,0,0,0.06);
}

.chart-card h3 {
  font-size: 14px;
  color: #333;
  margin-bottom: 12px;
  font-weight: 600;
}

.chart-card.full-width {
  grid-column: 1 / -1;
}
</style>
