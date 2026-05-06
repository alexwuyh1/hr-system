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
        <canvas :ref="el => chartRefs[chart.id] = el" :id="chart.id"></canvas>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive } from 'vue'
import { useDashboardStore } from '@/stores/dashboard'
import { useChart } from '@/composables/useChart'

const store = useDashboardStore()
const { render } = useChart()
const chartRefs = reactive({})

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

const CHART_COLORS = ['#e63946', '#f4a261', '#2a9d8f', '#264653', '#e9c46a', '#9b59b6']

function renderCharts() {
  if (!store.data) return

  const canvasStatus = chartRefs['chart-status']
  if (canvasStatus) {
    const sd = store.data.statusDistribution
    const total = sd.reduce((sum, i) => sum + i.value, 0)
    const statusColors = { '在职': '#10b981', '离职': '#ef4444' }
    render(canvasStatus,
      {
        labels: sd.map(i => i.name),
        datasets: [{
          data: sd.map(i => i.value),
          backgroundColor: sd.map(i => statusColors[i.name] || '#6d6255'),
          borderWidth: 0
        }]
      },
      {
        responsive: true,
        maintainAspectRatio: false,
        animation: { animateRotate: true, animateScale: true },
        plugins: {
          legend: { position: 'bottom', labels: { padding: 16, font: { size: 12 } } },
          tooltip: {
            callbacks: {
              label: ctx => {
                const pct = total > 0 ? ((ctx.parsed / total) * 100).toFixed(1) : 0
                return ` ${ctx.label}: ${ctx.parsed} 人 (${pct}%)`
              }
            }
          }
        }
      },
      'doughnut'
    )
  }

  const canvasDept = chartRefs['chart-dept']
  if (canvasDept) {
    const dd = store.data.departmentDistribution
    const total = dd.reduce((sum, i) => sum + i.value, 0)
    render(canvasDept,
      {
        labels: dd.map(i => i.name),
        datasets: [{
          data: dd.map(i => i.value),
          backgroundColor: CHART_COLORS.slice(0, dd.length),
          borderWidth: 0
        }]
      },
      {
        responsive: true,
        maintainAspectRatio: false,
        animation: { animateRotate: true, animateScale: true },
        plugins: {
          legend: { position: 'bottom', labels: { padding: 16, font: { size: 12 } } },
          tooltip: {
            callbacks: {
              label: ctx => {
                const pct = total > 0 ? ((ctx.parsed / total) * 100).toFixed(1) : 0
                return ` ${ctx.label}: ${ctx.parsed} 人 (${pct}%)`
              }
            }
          }
        }
      },
      'doughnut'
    )
  }

  const canvasAttendance = chartRefs['chart-attendance']
  if (canvasAttendance) {
    const ai = store.data.attendanceIssues
    render(canvasAttendance,
      {
        labels: ai.map(i => i.name),
        datasets: [{
          label: '异常次数',
          data: ai.map(i => i.value),
          backgroundColor: '#f97316',
          borderRadius: 4
        }]
      },
      {
        responsive: true,
        maintainAspectRatio: false,
        indexAxis: 'y',
        animation: { duration: 800 },
        scales: {
          x: {
            beginAtZero: true,
            title: { display: true, text: '异常次数', color: '#666' },
            ticks: { stepSize: 1, color: '#666' },
            grid: { color: '#f0f0f0' }
          },
          y: {
            ticks: { color: '#333', font: { size: 11 } },
            grid: { display: false }
          }
        },
        plugins: {
          legend: { display: false },
          tooltip: { callbacks: { label: ctx => ` ${ctx.parsed.x} 次` } }
        }
      },
      'bar'
    )
  }

  const canvasPayroll = chartRefs['chart-payroll']
  if (canvasPayroll) {
    const pd = store.data.payrollByDepartment
    render(canvasPayroll,
      {
        labels: pd.map(i => i.name),
        datasets: [{
          label: '薪资成本',
          data: pd.map(i => i.value),
          backgroundColor: '#3b82f6',
          borderRadius: 4
        }]
      },
      {
        responsive: true,
        maintainAspectRatio: false,
        animation: { duration: 800 },
        scales: {
          x: {
            title: { display: true, text: '部门', color: '#666' },
            ticks: { color: '#333', font: { size: 11 } },
            grid: { display: false }
          },
          y: {
            beginAtZero: true,
            title: { display: true, text: '金额（元）', color: '#666' },
            ticks: { callback: v => '¥' + (v >= 1000 ? (v / 1000).toFixed(0) + 'k' : v), color: '#666' },
            grid: { color: '#f0f0f0' }
          }
        },
        plugins: {
          legend: { display: false },
          tooltip: {
            callbacks: { label: ctx => ` ${ctx.dataset.label}: ¥${ctx.parsed.y.toLocaleString()}` }
          }
        }
      },
      'bar'
    )
  }
}

onMounted(async () => {
  await store.fetchSummary()
  renderCharts()
})
</script>