let dashboardCharts = {};

function initDashboard() {
  if (initCache.dashboard) {
    applyDashboard(initCache.dashboard);
  }
}

function ensureChart(id, config) {
  const canvas = $(id);
  if (!canvas || !window.Chart) {
    return null;
  }
  if (!dashboardCharts[id]) {
    dashboardCharts[id] = new Chart(canvas.getContext("2d"), config);
  }
  return dashboardCharts[id];
}

function updateChart(chart, labels, datasets) {
  if (!chart) return;
  chart.data.labels = labels;
  chart.data.datasets = datasets;
  chart.update();
}

async function loadDashboard() {
  const dashboard = await apiRequest(API.dashboard.summary);
  initCache.dashboard = dashboard;
  applyDashboard(dashboard);
}

function applyDashboard(dashboard) {
  if (!dashboard) return;
  setText("dash-total", dashboard.totalEmployees);
  setText("dash-active", dashboard.activeEmployees);
  setText("dash-attendance", dashboard.attendanceToday);
  setText("dash-payroll", dashboard.totalPayroll.toFixed(2));

  const dashboardTab = $("tab-dashboard");
  if (!dashboardTab || !dashboardTab.classList.contains("active")) {
    return;
  }

  const headcountLabels = dashboard.headcountTrend.map((i) => i.month);
  const headcountValues = dashboard.headcountTrend.map((i) => i.value);
  const headcountChart = ensureChart("chart-headcount", {
    type: "line",
    data: {
      labels: headcountLabels,
      datasets: [
        {
          label: "员工规模",
          data: headcountValues,
          borderColor: "#f48c06",
          backgroundColor: "rgba(244, 140, 6, 0.2)",
          tension: 0.3,
          fill: true,
        },
      ],
    },
    options: { responsive: true, maintainAspectRatio: false },
  });
  updateChart(headcountChart, headcountLabels, [
    {
      label: "员工规模",
      data: headcountValues,
      borderColor: "#f48c06",
      backgroundColor: "rgba(244, 140, 6, 0.2)",
      tension: 0.3,
      fill: true,
    },
  ]);

  const flowLabels = dashboard.flowTrend.map((i) => i.month);
  const flowHired = dashboard.flowTrend.map((i) => i.hired);
  const flowLeft = dashboard.flowTrend.map((i) => i.left);
  const flowChart = ensureChart("chart-flow", {
    type: "bar",
    data: {
      labels: flowLabels,
      datasets: [
        { label: "入职", data: flowHired, backgroundColor: "#f48c06" },
        { label: "离职", data: flowLeft, backgroundColor: "#6d6255" },
      ],
    },
    options: { responsive: true, maintainAspectRatio: false },
  });
  updateChart(flowChart, flowLabels, [
    { label: "入职", data: flowHired, backgroundColor: "#f48c06" },
    { label: "离职", data: flowLeft, backgroundColor: "#6d6255" },
  ]);

  const deptLabels = dashboard.departmentDistribution.map((i) => i.name);
  const deptValues = dashboard.departmentDistribution.map((i) => i.value);
  const deptChart = ensureChart("chart-dept", {
    type: "doughnut",
    data: {
      labels: deptLabels,
      datasets: [
        {
          data: deptValues,
          backgroundColor: ["#f48c06", "#ffb703", "#8ecae6", "#219ebc", "#023047", "#b5838d"],
        },
      ],
    },
    options: { responsive: true, maintainAspectRatio: false },
  });
  updateChart(deptChart, deptLabels, [
    {
      data: deptValues,
      backgroundColor: ["#f48c06", "#ffb703", "#8ecae6", "#219ebc", "#023047", "#b5838d"],
    },
  ]);

  const attendanceLabels = dashboard.attendanceIssues.map((i) => i.name);
  const attendanceValues = dashboard.attendanceIssues.map((i) => i.value);
  const attendanceChart = ensureChart("chart-attendance", {
    type: "bar",
    data: {
      labels: attendanceLabels,
      datasets: [
        { label: "异常次数", data: attendanceValues, backgroundColor: "#c76b00" },
      ],
    },
    options: {
      responsive: true,
      maintainAspectRatio: false,
      indexAxis: "y",
    },
  });
  updateChart(attendanceChart, attendanceLabels, [
    { label: "异常次数", data: attendanceValues, backgroundColor: "#c76b00" },
  ]);

  const payrollLabels = dashboard.payrollByDepartment.map((i) => i.name);
  const payrollValues = dashboard.payrollByDepartment.map((i) => i.value);
  const payrollChart = ensureChart("chart-payroll", {
    type: "bar",
    data: {
      labels: payrollLabels,
      datasets: [
        { label: "薪资成本", data: payrollValues, backgroundColor: "#8ecae6" },
      ],
    },
    options: { responsive: true, maintainAspectRatio: false },
  });
  updateChart(payrollChart, payrollLabels, [
    { label: "薪资成本", data: payrollValues, backgroundColor: "#8ecae6" },
  ]);
}
