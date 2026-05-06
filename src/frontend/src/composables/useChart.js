import { ref, onUnmounted } from 'vue'
import Chart from 'chart.js/auto'

export function useChart() {
  const chartInstances = {}

  function render(canvas, data, options, type = 'doughnut') {
    const ctx = canvas.getContext('2d')
    if (chartInstances[canvas.id]) {
      chartInstances[canvas.id].destroy()
    }
    chartInstances[canvas.id] = new Chart(ctx, { type, data, options })
    return chartInstances[canvas.id]
  }

  function update(canvas, data, options) {
    const instance = chartInstances[canvas.id]
    if (!instance) return
    if (data) instance.data = data
    if (options) Object.assign(instance.options, options)
    instance.update()
  }

  function destroy(canvas) {
    if (chartInstances[canvas.id]) {
      chartInstances[canvas.id].destroy()
      delete chartInstances[canvas.id]
    }
  }

  function destroyAll() {
    Object.values(chartInstances).forEach(c => c.destroy())
    Object.keys(chartInstances).forEach(k => delete chartInstances[k])
  }

  onUnmounted(() => {
    destroyAll()
  })

  return { render, update, destroy, destroyAll, instances: chartInstances }
}