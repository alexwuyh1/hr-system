const API_BASE = "/api";

// Store auth token in memory for this demo.
let authToken = null;
let dashboardCharts = {};

const $ = (id) => document.getElementById(id);

async function apiRequest(path, options = {}) {
  const headers = options.headers || {};
  if (authToken) {
    headers.Authorization = `Bearer ${authToken}`;
  }
  const response = await fetch(`${API_BASE}${path}`, {
    ...options,
    headers: {
      "Content-Type": "application/json",
      ...headers,
    },
  });
  if (!response.ok) {
    const error = await response.text();
    throw new Error(error || "Request failed");
  }
  if (response.status === 204) {
    return null;
  }
  return response.json();
}

function switchTab(name) {
  document.querySelectorAll(".tabs button").forEach((btn) => {
    btn.classList.toggle("active", btn.dataset.tab === name);
  });
  document.querySelectorAll(".tab-content").forEach((tab) => {
    tab.classList.toggle("active", tab.id === `tab-${name}`);
  });
}

function buildRow(cells, onDelete) {
  const tr = document.createElement("tr");
  cells.forEach((text) => {
    const td = document.createElement("td");
    td.textContent = text;
    tr.appendChild(td);
  });
  const actionTd = document.createElement("td");
  const delBtn = document.createElement("button");
  delBtn.textContent = "删除";
  delBtn.className = "ghost";
  delBtn.onclick = onDelete;
  actionTd.appendChild(delBtn);
  tr.appendChild(actionTd);
  return tr;
}

function renderTree(nodes) {
  const ul = document.createElement("ul");
  ul.className = "tree";
  nodes.forEach((node) => {
    const li = document.createElement("li");
    const nodeWrap = document.createElement("div");
    nodeWrap.className = "tree-node";

    let children = null;
    if (node.children && node.children.length > 0) {
      const toggle = document.createElement("span");
      toggle.className = "tree-toggle";
      toggle.textContent = "-";
      toggle.onclick = () => {
        children.classList.toggle("hidden");
        toggle.textContent = children.classList.contains("hidden") ? "+" : "-";
      };
      nodeWrap.appendChild(toggle);
    } else {
      const spacer = document.createElement("span");
      spacer.className = "tree-toggle";
      spacer.textContent = "•";
      spacer.style.borderColor = "transparent";
      spacer.style.cursor = "default";
      nodeWrap.appendChild(spacer);
    }

    const label = document.createElement("span");
    label.textContent = node.name;
    nodeWrap.appendChild(label);
    li.appendChild(nodeWrap);

    if (node.children && node.children.length > 0) {
      children = document.createElement("div");
      children.className = "tree-children";
      children.appendChild(renderTree(node.children));
      li.appendChild(children);
    }
    ul.appendChild(li);
  });
  return ul;
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

async function loadReport() {
  const report = await apiRequest("/reports/summary");
  $("stat-total").textContent = report.totalEmployees;
  $("stat-attendance").textContent = report.attendanceToday;
  $("stat-payroll").textContent = report.totalPayroll.toFixed(2);
  $("report-total").textContent = report.totalEmployees;
  $("report-attendance").textContent = report.attendanceToday;
  $("report-payroll").textContent = report.totalPayroll.toFixed(2);
}

async function loadDashboard() {
  const dashboard = await apiRequest("/dashboard/summary");
  $("dash-total").textContent = dashboard.totalEmployees;
  $("dash-active").textContent = dashboard.activeEmployees;
  $("dash-attendance").textContent = dashboard.attendanceToday;
  $("dash-payroll").textContent = dashboard.totalPayroll.toFixed(2);

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

async function loadRoles() {
  const roles = await apiRequest("/roles");
  const select = $("perm-role");
  if (!select) return;
  select.innerHTML = "";
  roles.forEach((r) => {
    const option = document.createElement("option");
    option.value = r.name;
    option.textContent = r.name;
    select.appendChild(option);
  });

  const roleTable = $("role-table");
  if (roleTable) {
    const body = roleTable.querySelector("tbody");
    body.innerHTML = "";
    roles.forEach((r) => {
      const tr = document.createElement("tr");
      const td = document.createElement("td");
      td.textContent = r.name;
      tr.appendChild(td);
      body.appendChild(tr);
    });
  }
}

async function loadPermissions() {
  const list = await apiRequest("/permissions");
  const body = $("perm-table").querySelector("tbody");
  body.innerHTML = "";
  list.forEach((p) => {
    const row = buildRow([p.role, p.method, p.pathPrefix], async () => {
      await apiRequest(`/permissions/${p.id}`, { method: "DELETE" });
      await loadPermissions();
    });
    body.appendChild(row);
  });
}

async function loadEmployees() {
  const list = await apiRequest("/employees");
  const body = $("employee-table").querySelector("tbody");
  body.innerHTML = "";
  list.forEach((e) => {
    const row = buildRow(
      [e.employeeNo, e.name, e.departmentName || e.department, e.positionName || e.title, e.status],
      async () => {
        await apiRequest(`/employees/${e.id}`, { method: "DELETE" });
        await loadEmployees();
      }
    );
    body.appendChild(row);
  });

  // Populate profile selector
  const profileSelect = $("profile-employee");
  if (profileSelect) {
    profileSelect.innerHTML = "";
    list.forEach((e) => {
      const option = document.createElement("option");
      option.value = e.id;
      option.textContent = `${e.employeeNo} - ${e.name}`;
      option.dataset.avatar = e.avatarUrl || "";
      option.dataset.info = JSON.stringify(e);
      profileSelect.appendChild(option);
    });
    if (list.length > 0) {
      renderProfile(list[0]);
    }
  }
}

async function loadAttendance() {
  const list = await apiRequest("/attendance");
  const body = $("attendance-table").querySelector("tbody");
  body.innerHTML = "";
  list.forEach((a) => {
    const row = buildRow(
      [
        a.employee?.name || a.employee?.employeeNo || a.employee?.id || "-",
        a.workDate,
        a.checkIn || "-",
        a.checkOut || "-",
        a.status,
      ],
      async () => {
        await apiRequest(`/attendance/${a.id}`, { method: "DELETE" });
        await loadAttendance();
      }
    );
    body.appendChild(row);
  });
}

async function loadSalary() {
  const list = await apiRequest("/salaries");
  const body = $("salary-table").querySelector("tbody");
  body.innerHTML = "";
  list.forEach((s) => {
    const row = buildRow(
      [
        s.employee?.name || s.employee?.employeeNo || s.employee?.id || "-",
        s.salaryMonth,
        s.baseSalary,
        s.bonus,
        s.deduction,
      ],
      async () => {
        await apiRequest(`/salaries/${s.id}`, { method: "DELETE" });
        await loadSalary();
      }
    );
    body.appendChild(row);
  });
}

async function loadDepartments() {
  const list = await apiRequest("/departments");
  const select = $("employee-department");
  if (select) {
    select.innerHTML = "";
    list.forEach((d) => {
      const option = document.createElement("option");
      option.value = d.id;
      option.textContent = d.name;
      select.appendChild(option);
    });
  }

  const parentSelect = $("dept-parent");
  if (parentSelect) {
    parentSelect.innerHTML = "<option value=\"\">无</option>";
    list.forEach((d) => {
      const option = document.createElement("option");
      option.value = d.id;
      option.textContent = d.name;
      parentSelect.appendChild(option);
    });
  }

  const body = $("dept-table").querySelector("tbody");
  body.innerHTML = "";
  list.forEach((d) => {
    const row = buildRow(
      [d.name, d.parent ? d.parent.name : "-"],
      async () => {
        await apiRequest(`/departments/${d.id}`, { method: "DELETE" });
        await loadDepartments();
      }
    );
    body.appendChild(row);
  });

  const treeContainer = $("dept-tree");
  if (treeContainer) {
    const treeData = await apiRequest("/departments/tree");
    treeContainer.innerHTML = "";
    treeContainer.appendChild(renderTree(treeData));
  }
}

async function loadPositions() {
  const list = await apiRequest("/positions");
  const select = $("employee-position");
  if (select) {
    select.innerHTML = "";
    list.forEach((p) => {
      const option = document.createElement("option");
      option.value = p.id;
      option.textContent = p.name;
      select.appendChild(option);
    });
  }

  const body = $("pos-table").querySelector("tbody");
  body.innerHTML = "";
  list.forEach((p) => {
    const row = buildRow([p.name], async () => {
      await apiRequest(`/positions/${p.id}`, { method: "DELETE" });
      await loadPositions();
    });
    body.appendChild(row);
  });
}

async function loadGrades() {
  const list = await apiRequest("/grades");
  const select = $("employee-grade");
  if (select) {
    select.innerHTML = "";
    list.forEach((g) => {
      const option = document.createElement("option");
      option.value = g.id;
      option.textContent = `${g.name} (L${g.level})`;
      select.appendChild(option);
    });
  }

  const body = $("grade-table").querySelector("tbody");
  body.innerHTML = "";
  list.forEach((g) => {
    const row = buildRow([g.name, g.level], async () => {
      await apiRequest(`/grades/${g.id}`, { method: "DELETE" });
      await loadGrades();
    });
    body.appendChild(row);
  });
}

async function loadAttendanceRule() {
  const rule = await apiRequest("/attendance-rules");
  const lateInput = $("rule-late");
  const overtimeInput = $("rule-overtime");
  if (lateInput) lateInput.value = rule.lateGraceMinutes;
  if (overtimeInput) overtimeInput.value = rule.overtimeThresholdMinutes;
}

function renderProfile(employee) {
  const info = $("profile-info");
  if (!info) return;
  const avatar = $("profile-avatar-img");
  avatar.src = employee.avatarUrl || "";
  avatar.alt = employee.name || "avatar";
  info.innerHTML = `
    <div><strong>${employee.name}</strong> <span>(${employee.employeeNo})</span></div>
    <div>部门：${employee.departmentName || employee.department || "-"}</div>
    <div>岗位：${employee.positionName || employee.title || "-"}</div>
    <div>职级：${employee.gradeName || "-"}</div>
    <div>直属上级：${employee.managerName || "-"}</div>
    <div>邮箱：${employee.email || "-"}</div>
    <div>电话：${employee.phone || "-"}</div>
  `;
}

// Login flow
$("login-form").addEventListener("submit", async (e) => {
  e.preventDefault();
  const data = Object.fromEntries(new FormData(e.target));
  try {
    const result = await apiRequest("/auth/login", {
      method: "POST",
      body: JSON.stringify(data),
    });
    authToken = result.token;
    $("auth-panel").classList.add("hidden");
    $("workspace").classList.remove("hidden");
    await Promise.all([
      loadDashboard().catch(() => {}),
      loadEmployees(),
      loadAttendance(),
      loadSalary(),
      loadReport(),
      loadDepartments().catch(() => {}),
      loadPositions().catch(() => {}),
      loadGrades().catch(() => {}),
      loadAttendanceRule().catch(() => {}),
      loadRoles().catch(() => {}),
      loadPermissions().catch(() => {}),
    ]);
  } catch (err) {
    alert(`登录失败: ${err.message}`);
  }
});

$("register-btn").addEventListener("click", async () => {
  const username = prompt("输入新用户名");
  const password = prompt("输入新密码");
  if (!username || !password) {
    return;
  }
  try {
    await apiRequest("/auth/register", {
      method: "POST",
      body: JSON.stringify({ username, password }),
    });
    alert("注册成功，请登录");
  } catch (err) {
    alert(`注册失败: ${err.message}`);
  }
});

$("logout").addEventListener("click", () => {
  authToken = null;
  $("workspace").classList.add("hidden");
  $("auth-panel").classList.remove("hidden");
});

// Employee form
$("employee-form").addEventListener("submit", async (e) => {
  e.preventDefault();
  const data = Object.fromEntries(new FormData(e.target));
  const deptSelect = $("employee-department");
  const posSelect = $("employee-position");
  const gradeSelect = $("employee-grade");
  data.departmentId = deptSelect ? Number(deptSelect.value) : null;
  data.positionId = posSelect ? Number(posSelect.value) : null;
  data.gradeId = gradeSelect ? Number(gradeSelect.value) : null;
  data.managerId = data.managerId ? Number(data.managerId) : null;
  // Sync legacy text fields for compatibility
  data.department = deptSelect ? deptSelect.options[deptSelect.selectedIndex].textContent : data.department;
  data.title = posSelect ? posSelect.options[posSelect.selectedIndex].textContent : data.title;
  try {
    await apiRequest("/employees", {
      method: "POST",
      body: JSON.stringify(data),
    });
    e.target.reset();
    await loadEmployees();
  } catch (err) {
    alert(`保存失败: ${err.message}`);
  }
});

// Profile interactions
const profileSelect = $("profile-employee");
if (profileSelect) {
  profileSelect.addEventListener("change", () => {
    const option = profileSelect.options[profileSelect.selectedIndex];
    const employee = JSON.parse(option.dataset.info);
    renderProfile(employee);
  });
}

const profileUploadBtn = $("profile-upload");
if (profileUploadBtn) {
  profileUploadBtn.addEventListener("click", async () => {
    const employeeId = $("profile-employee").value;
    const file = $("profile-avatar").files[0];
    if (!file) {
      alert("请选择头像文件");
      return;
    }
    const form = new FormData();
    form.append("file", file);
    const response = await fetch(`/api/employees/${employeeId}/avatar`, {
      method: "POST",
      headers: authToken ? { Authorization: `Bearer ${authToken}` } : {},
      body: form,
    });
    if (!response.ok) {
      const err = await response.text();
      $("profile-result").textContent = `上传失败：${err}`;
      return;
    }
    $("profile-result").textContent = "头像上传成功";
    await loadEmployees();
    const selected = $("profile-employee").value;
    const option = Array.from($("profile-employee").options).find((o) => o.value === selected);
    if (option) {
      renderProfile(JSON.parse(option.dataset.info));
    }
  });
}

const profileVerifyBtn = $("profile-verify-btn");
if (profileVerifyBtn) {
  profileVerifyBtn.addEventListener("click", async () => {
    const employeeId = $("profile-employee").value;
    const file = $("profile-verify").files[0];
    if (!file) {
      alert("请选择识别文件");
      return;
    }
    const form = new FormData();
    form.append("employeeId", employeeId);
    form.append("file", file);
    const response = await fetch("/api/face/verify", {
      method: "POST",
      headers: authToken ? { Authorization: `Bearer ${authToken}` } : {},
      body: form,
    });
    const data = await response.json();
    $("profile-result").textContent = data.matched
      ? `验证通过，距离 ${data.distance}`
      : `验证失败，距离 ${data.distance}`;
  });
}

const profileCheckinBtn = $("profile-checkin");
if (profileCheckinBtn) {
  profileCheckinBtn.addEventListener("click", async () => {
    const employeeId = $("profile-employee").value;
    const file = $("profile-verify").files[0];
    if (!file) {
      alert("请先选择识别文件");
      return;
    }
    const form = new FormData();
    form.append("employeeId", employeeId);
    form.append("file", file);
    const response = await fetch("/api/face/checkin", {
      method: "POST",
      headers: authToken ? { Authorization: `Bearer ${authToken}` } : {},
      body: form,
    });
    if (!response.ok) {
      const err = await response.text();
      $("profile-result").textContent = `考勤失败：${err}`;
      return;
    }
    const data = await response.json();
    $("profile-result").textContent = `考勤成功：${data.workDate} ${data.checkIn || ""} ${data.checkOut || ""}`;
    await loadAttendance();
  });
}

// Attendance form
$("attendance-form").addEventListener("submit", async (e) => {
  e.preventDefault();
  const data = Object.fromEntries(new FormData(e.target));
  data.employeeId = Number(data.employeeId);
  try {
    await apiRequest("/attendance", {
      method: "POST",
      body: JSON.stringify(data),
    });
    e.target.reset();
    await loadAttendance();
  } catch (err) {
    alert(`保存失败: ${err.message}`);
  }
});

// Salary form
$("salary-form").addEventListener("submit", async (e) => {
  e.preventDefault();
  const data = Object.fromEntries(new FormData(e.target));
  data.employeeId = Number(data.employeeId);
  data.baseSalary = Number(data.baseSalary);
  data.bonus = Number(data.bonus);
  data.deduction = Number(data.deduction);
  try {
    await apiRequest("/salaries", {
      method: "POST",
      body: JSON.stringify(data),
    });
    e.target.reset();
    await loadSalary();
  } catch (err) {
    alert(`保存失败: ${err.message}`);
  }
});

// Tabs
document.querySelectorAll(".tabs button[data-tab]").forEach((btn) => {
  btn.addEventListener("click", async () => {
    switchTab(btn.dataset.tab);
    if (btn.dataset.tab === "dashboard") {
      await loadDashboard();
    }
  });
});

// Report refresh
$("refresh-report").addEventListener("click", async () => {
  await loadReport();
});

// Attendance rule management
const ruleSaveBtn = $("rule-save");
if (ruleSaveBtn) {
  ruleSaveBtn.addEventListener("click", async () => {
    const late = Number($("rule-late").value);
    const overtime = Number($("rule-overtime").value);
    if (Number.isNaN(late) || Number.isNaN(overtime)) {
      alert("请输入有效数字");
      return;
    }
    const result = await apiRequest("/attendance-rules", {
      method: "PUT",
      body: JSON.stringify({ lateGraceMinutes: late, overtimeThresholdMinutes: overtime }),
    });
    $("rule-result").textContent = `规则已保存：迟到宽限 ${result.lateGraceMinutes} 分钟，加班阈值 ${result.overtimeThresholdMinutes} 分钟`;
  });
}

const ruleCalcBtn = $("rule-calc");
if (ruleCalcBtn) {
  ruleCalcBtn.addEventListener("click", async () => {
    const date = $("rule-date").value;
    if (!date) {
      alert("请选择日期");
      return;
    }
    const result = await apiRequest(`/attendance-rules/calculate?date=${date}`, { method: "POST" });
    $("rule-result").textContent = `已计算 ${date}，更新 ${result.updated} 条记录`;
    await loadAttendance();
  });
}

const ruleCalcRangeBtn = $("rule-calc-range");
if (ruleCalcRangeBtn) {
  ruleCalcRangeBtn.addEventListener("click", async () => {
    const start = $("rule-start").value;
    const end = $("rule-end").value;
    if (!start || !end) {
      alert("请选择起止日期");
      return;
    }
    const result = await apiRequest(
      `/attendance-rules/calculate-range?start=${start}&end=${end}`,
      { method: "POST" }
    );
    $("rule-result").textContent = `已计算 ${start} 到 ${end}，更新 ${result.updated} 条记录`;
    await loadAttendance();
  });
}

// Import/Export
const dataImportBtn = $("data-import");
if (dataImportBtn) {
  dataImportBtn.addEventListener("click", async () => {
    const type = $("data-type").value;
    const format = $("data-format").value;
    const fileInput = $("data-file");
    const file = fileInput.files[0];
    if (!file) {
      alert("请选择文件");
      return;
    }
    const form = new FormData();
    form.append("file", file);
    try {
      const response = await fetch(`/api/data/import/${type}?format=${format}`, {
        method: "POST",
        headers: authToken ? { Authorization: `Bearer ${authToken}` } : {},
        body: form,
      });
      if (!response.ok) {
        const error = await response.text();
        throw new Error(error || "导入失败");
      }
      const result = await response.json();
      $("data-result").textContent = `导入成功：${result.imported} 条`;
    } catch (err) {
      $("data-result").textContent = `导入失败：${err.message}`;
    }
  });
}

const dataExportBtn = $("data-export");
if (dataExportBtn) {
  dataExportBtn.addEventListener("click", async () => {
    const type = $("data-type").value;
    const format = $("data-format").value;
    const url = `/api/data/export/${type}?format=${format}`;
    const a = document.createElement("a");
    a.href = url;
    a.target = "_blank";
    a.rel = "noopener";
    a.click();
  });
}

// Org management
const deptAddBtn = $("dept-add");
if (deptAddBtn) {
  deptAddBtn.addEventListener("click", async () => {
    const name = $("dept-name").value.trim();
    const parentId = $("dept-parent").value;
    if (!name) {
      alert("请输入部门名称");
      return;
    }
    await apiRequest("/departments", {
      method: "POST",
      body: JSON.stringify({ name, parentId: parentId ? Number(parentId) : null }),
    });
    $("dept-name").value = "";
    await loadDepartments();
  });
}

const posAddBtn = $("pos-add");
if (posAddBtn) {
  posAddBtn.addEventListener("click", async () => {
    const name = $("pos-name").value.trim();
    if (!name) {
      alert("请输入岗位名称");
      return;
    }
    await apiRequest("/positions", {
      method: "POST",
      body: JSON.stringify({ name }),
    });
    $("pos-name").value = "";
    await loadPositions();
  });
}

const gradeAddBtn = $("grade-add");
if (gradeAddBtn) {
  gradeAddBtn.addEventListener("click", async () => {
    const name = $("grade-name").value.trim();
    const level = Number($("grade-level").value);
    if (!name || Number.isNaN(level)) {
      alert("请输入职级名称和等级");
      return;
    }
    await apiRequest("/grades", {
      method: "POST",
      body: JSON.stringify({ name, level }),
    });
    $("grade-name").value = "";
    $("grade-level").value = "";
    await loadGrades();
  });
}

// Permissions form
const permAddBtn = $("perm-add");
if (permAddBtn) {
  permAddBtn.addEventListener("click", async () => {
    try {
      const role = $("perm-role").value;
      const method = $("perm-method").value;
      const pathPrefix = $("perm-path").value.trim();
      if (!pathPrefix) {
        alert("请输入路径前缀");
        return;
      }
      await apiRequest("/permissions", {
        method: "POST",
        body: JSON.stringify({ role, method, pathPrefix }),
      });
      $("perm-path").value = "";
      await loadPermissions();
    } catch (err) {
      alert(`新增失败: ${err.message}`);
    }
  });
}
