<template>
  <div>
    <div class="panel">
      <div class="panel-header">
        <h2>管理层仪表盘</h2>
        <p>从人力、考勤与薪酬角度快速洞察组织状态</p>
      </div>
      <div class="dashboard-stats">
        <div class="stat" v-for="s in dashboardStats" :key="s.label">
          <label>{{ s.label }}</label>
          <span class="value">{{ s.value }}</span>
        </div>
      </div>
    </div>
    <div class="dashboard-grid">
      <div v-for="chart in chartDefs" :key="chart.id" class="panel chart-panel">
        <h3>{{ chart.title }}</h3>
        <canvas :id="chart.id"></canvas>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, onUnmounted } from 'vue'
import { useDashboardStore } from '@/stores/dashboard'
import Chart from 'chart.js/auto'

const store = useDashboardStore()
const chartInstances = {}

const dashboardStats = computed(() => {
  if (!store.data) return []
  return [
    { label: '在岗员工', value: store.data.activeEmployees },
    { label: '员工总数', value: store.data.totalEmployees },
    { label: '今日考勤', value: store.data.attendanceToday },
    { label: '薪资成本', value: '¥' + Number(store.data.totalPayroll).toLocaleString() }
  ]
})

const chartDefs = [
  { id: 'chart-status', title: '员工状态分布' },
  { id: 'chart-dept', title: '部门人数分布' },
  { id: 'chart-attendance', title: '考勤异常排行（近30天）' },
  { id: 'chart-payroll', title: '部门人力成本' }
]

const CHART_COLORS = ['#f48c06', '#ffb703', '#8ecae6', '#219ebc', '#023047', '#b5838d']

function $(id) { return document.getElementById(id) }

function renderChart(id, labels, datasets, type, extra) {
  const canvas = $(id)
  if (!canvas) return
  if (!chartInstances[id]) {
    const config = { type, data: { labels, datasets }, options: { responsive: true, maintainAspectRatio: false, ...extra } }
    chartInstances[id] = new Chart(canvas.getContext('2d'), config)
  } else {
    const chart = chartInstances[id]
    chart.data.labels = labels
    chart.data.datasets = datasets
    chart.update()
  }
}

function renderCharts() {
  if (!store.data) return

  const sd = store.data.statusDistribution
  const statusColors = { '在职': '#16a34a', '离职': '#dc2626' }
  renderChart('chart-status', sd.map(i => i.name),
    [{ data: sd.map(i => i.value), backgroundColor: sd.map(i => statusColors[i.name] || '#6d6255') }],
    'doughnut', {
      plugins: { tooltip: { callbacks: { label: ctx => ` ${ctx.label}: ${ctx.parsed} 人` } } }
    })

  const dd = store.data.departmentDistribution
  renderChart('chart-dept', dd.map(i => i.name),
    [{ data: dd.map(i => i.value), backgroundColor: CHART_COLORS }],
    'doughnut', {
      plugins: { tooltip: { callbacks: { label: ctx => ` ${ctx.label}: ${ctx.parsed} 人` } } }
    })

  const ai = store.data.attendanceIssues
  renderChart('chart-attendance', ai.map(i => i.name),
    [{ label: '异常次数', data: ai.map(i => i.value), backgroundColor: '#c76b00' }],
    'bar', {
      indexAxis: 'y',
      scales: { x: { beginAtZero: true, title: { display: true, text: '异常次数' }, ticks: { stepSize: 1 } } },
      plugins: { tooltip: { callbacks: { label: ctx => ` ${ctx.parsed.x} 次` } } }
    })

  const pd = store.data.payrollByDepartment
  renderChart('chart-payroll', pd.map(i => i.name),
    [{ label: '薪资成本', data: pd.map(i => i.value), backgroundColor: '#8ecae6' }],
    'bar', {
      scales: { y: { beginAtZero: true, title: { display: true, text: '金额（元）' }, ticks: { callback: v => '¥' + v.toLocaleString() } } },
      plugins: { tooltip: { callbacks: { label: ctx => ` ${ctx.dataset.label}: ¥${ctx.parsed.y.toLocaleString()}` } } }
    })
}

onMounted(async () => {
  await store.fetchSummary()
  renderCharts()
})

onUnmounted(() => {
  Object.values(chartInstances).forEach(c => c.destroy())
})
</script>
