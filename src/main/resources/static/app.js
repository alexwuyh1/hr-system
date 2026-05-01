const API_BASE = "/api";

// Store auth token in memory and localStorage for persistence.
let authToken = localStorage.getItem("hr_token");
let dashboardCharts = {};
let initCache = {
  dashboard: null,
  attendanceRule: null,
  departments: null,
  departmentTree: null,
  positions: null,
  grades: null,
  roles: null,
  permissions: null,
};
const loadedTabs = new Set();

const $ = (id) => document.getElementById(id);

function setText(id, value) {
  const el = $(id);
  if (el) {
    el.textContent = value;
  }
}

function toMinutes(timeStr) {
  if (!timeStr) return null;
  const parts = timeStr.split(":");
  if (parts.length < 2) return null;
  const hours = Number(parts[0]);
  const minutes = Number(parts[1]);
  if (Number.isNaN(hours) || Number.isNaN(minutes)) return null;
  return hours * 60 + minutes;
}

function normalizeEmptyToNull(value) {
  return value === "" ? null : value;
}

function normalizeSalaryMonth(value) {
  if (!value) return value;
  if (value.length >= 7) {
    return value.slice(0, 7);
  }
  return value;
}

function validateAttendancePayload(data) {
  if (!data.employeeId || Number.isNaN(data.employeeId)) {
    return "请选择员工";
  }
  if (!data.workDate) {
    return "请选择日期";
  }
  if (!data.status) {
    return "请选择状态";
  }
  if (data.checkOut && !data.checkIn) {
    return "签退需要先填写签到时间";
  }
  const checkInMin = toMinutes(data.checkIn);
  const checkOutMin = toMinutes(data.checkOut);
  if (checkInMin != null && checkOutMin != null && checkOutMin < checkInMin) {
    return "签退时间不能早于签到时间";
  }
  return null;
}

function validateSalaryPayload(data) {
  if (!data.employeeId || Number.isNaN(data.employeeId)) {
    return "请选择员工";
  }
  if (!data.salaryMonth || !/^\d{4}-\d{2}$/.test(data.salaryMonth)) {
    return "薪资日期格式应为 YYYY-MM";
  }
  if ([data.baseSalary, data.bonus, data.deduction].some((v) => Number.isNaN(v))) {
    return "薪资金额需要填写数值";
  }
  if (data.baseSalary < 0 || data.bonus < 0 || data.deduction < 0) {
    return "薪资金额不能为负数";
  }
  if (data.baseSalary + data.bonus - data.deduction < 0) {
    return "薪资总额不能为负数";
  }
  return null;
}

async function safeLoad(label, fn) {
  try {
    return await fn();
  } catch (err) {
    console.warn(`[load:${label}]`, err);
    return null;
  }
}

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
  const contentType = response.headers.get("content-type") || "";
  if (!response.ok) {
    const error = await response.text();
    throw new Error(error || "Request failed");
  }
  if (response.status === 204) {
    return null;
  }
  const text = await response.text();
  if (!text) {
    return null;
  }
  if (contentType.includes("application/json")) {
    return JSON.parse(text);
  }
  return text;
}

function persistToken(token) {
  authToken = token;
  if (token) {
    localStorage.setItem("hr_token", token);
  } else {
    localStorage.removeItem("hr_token");
  }
}

function switchTab(name) {
  document.querySelectorAll(".tabs button").forEach((btn) => {
    btn.classList.toggle("active", btn.dataset.tab === name);
  });
  document.querySelectorAll(".tab-content").forEach((tab) => {
    tab.classList.toggle("active", tab.id === `tab-${name}`);
  });
}

function buildRow(cells, onDelete, extraActions = []) {
  const tr = document.createElement("tr");
  cells.forEach((text) => {
    const td = document.createElement("td");
    td.textContent = text;
    tr.appendChild(td);
  });
  const actionTd = document.createElement("td");
  extraActions.forEach((action) => actionTd.appendChild(action));
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

async function loadDashboard() {
  const dashboard = await apiRequest("/dashboard/summary");
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

function applyRoles(roles) {
  if (!roles) return;
  const select = $("perm-role-select");
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

async function loadRoles() {
  const roles = await apiRequest("/roles");
  initCache.roles = roles;
  applyRoles(roles);
}

function applyPermissions(list) {
  if (!list) return;
  const body = $("perm-table").querySelector("tbody");
  body.innerHTML = "";
  list.forEach((p) => {
    const row = buildRow([p.role, p.method, p.pathPrefix], async () => {
      await apiRequest(`/permissions/${p.id}`, { method: "DELETE" });
      await safeLoad("permissions", loadPermissions);
    });
    body.appendChild(row);
  });
}

async function loadPermissions() {
  const list = await apiRequest("/permissions");
  initCache.permissions = list;
  applyPermissions(list);
}

async function loadEmployees() {
  const list = await apiRequest("/employees");
  initCache.employees = list;
  applyEmployees(list);

  // Populate profile selector
  const profileSelect = $("profile-employee");
  if (profileSelect) {
    profileSelect.innerHTML = "";
    list.filter((e) => e.status === "在职").forEach((e) => {
      const option = document.createElement("option");
      option.value = e.id;
      option.textContent = `${e.employeeNo} - ${e.name}`;
      option.dataset.avatar = e.avatarUrl || "";
      option.dataset.info = JSON.stringify(e);
      profileSelect.appendChild(option);
    });
    if (profileSelect.options.length > 0) {
      renderProfile(JSON.parse(profileSelect.options[0].dataset.info));
    }
  }

  const attendanceSelect = $("attendance-employee");
  if (attendanceSelect) {
    attendanceSelect.innerHTML = "";
    list.filter((e) => e.status === "在职").forEach((e) => {
      const option = document.createElement("option");
      option.value = e.id;
      option.textContent = `${e.employeeNo} - ${e.name}`;
      attendanceSelect.appendChild(option);
    });
  }

  const salarySelect = $("salary-employee");
  if (salarySelect) {
    salarySelect.innerHTML = "";
    list.forEach((e) => {
      const option = document.createElement("option");
      option.value = e.id;
      option.textContent = `${e.employeeNo} - ${e.name}`;
      salarySelect.appendChild(option);
    });
  }
}

function applyEmployees(list) {
  if (!list) return;
  const body = $("employee-table").querySelector("tbody");
  body.innerHTML = "";
  list.forEach((e) => {
    const statusBtn = document.createElement("button");
    const isActive = e.status === "在职";
    statusBtn.textContent = isActive ? "离职" : "复职";
    statusBtn.className = "ghost";
    statusBtn.onclick = async () => {
      const endpoint = isActive ? "/employees/resign" : "/employees/rehire";
      await apiRequest(endpoint, {
        method: "POST",
        body: JSON.stringify({ employeeNo: e.employeeNo }),
      });
      await safeLoad("employees", loadEmployees);
    };
    const row = buildRow(
      [e.employeeNo, e.name, e.departmentName || e.department, e.positionName || e.title, e.status],
      async () => {
        await apiRequest(`/employees/${e.id}`, { method: "DELETE" });
        await safeLoad("employees", loadEmployees);
      },
      [statusBtn]
    );
    body.appendChild(row);
  });
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
        await safeLoad("attendance", loadAttendance);
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
        await safeLoad("salary", loadSalary);
      }
    );
    body.appendChild(row);
  });
}

async function loadDepartments() {
  const list = await apiRequest("/departments");
  initCache.departments = list;
  let treeData = initCache.departmentTree;
  if (!treeData) {
    treeData = await apiRequest("/departments/tree");
    initCache.departmentTree = treeData;
  }
  applyDepartments(list, treeData);
}

function applyDepartments(list, treeData) {
  if (!list) return;
  const select = $("employee-dept");
  if (select) {
    select.innerHTML = "";
    list.forEach((d) => {
      const option = document.createElement("option");
      option.value = d.id;
      option.textContent = d.name;
      select.appendChild(option);
    });
  }

  const parentSelect = $("dept-parent-select");
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
  if (treeContainer && treeData) {
    treeContainer.innerHTML = "";
    treeContainer.appendChild(renderTree(treeData));
  }
}

async function loadPositions() {
  const list = await apiRequest("/positions");
  initCache.positions = list;
  applyPositions(list);
}

function applyPositions(list) {
  if (!list) return;
  const select = $("employee-pos");
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
  initCache.grades = list;
  applyGrades(list);
}

function applyGrades(list) {
  if (!list) return;
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
  initCache.attendanceRule = rule;
  applyAttendanceRule(rule);
}

function applyAttendanceRule(rule) {
  if (!rule) return;
  const setVal = (id, val) => { const el = $(id); if (el) el.value = val ?? ""; };
  setVal("rule-start-time", rule.workStartTime);
  setVal("rule-end-time", rule.workEndTime);
  setVal("rule-lunch-start", rule.lunchBreakStart);
  setVal("rule-lunch-end", rule.lunchBreakEnd);
  setVal("rule-late", rule.lateGraceMinutes);
  setVal("rule-early-leave", rule.earlyLeaveGraceMinutes);
  setVal("rule-absent", rule.absentThresholdMinutes);
  setVal("rule-overtime", rule.overtimeThresholdMinutes);
  const approval = $("rule-ot-approval");
  if (approval) approval.checked = rule.requireOvertimeApproval === true;
}

async function loadInit() {
  const init = await apiRequest("/init");
  initCache = {
    ...initCache,
    ...init,
  };
  applyDashboard(init.dashboard);
  applyAttendanceRule(init.attendanceRule);
  applyDepartments(init.departments, init.departmentTree);
  applyPositions(init.positions);
  applyGrades(init.grades);
  applyRoles(init.roles);
  applyPermissions(init.permissions);
}

async function ensureTabData(tabName) {
  if (loadedTabs.has(tabName)) {
    return;
  }
  loadedTabs.add(tabName);
  if (tabName === "dashboard") {
    if (initCache.dashboard) {
      applyDashboard(initCache.dashboard);
    } else {
      await safeLoad("dashboard", loadDashboard);
    }
  }
  if (tabName === "employees") {
    await safeLoad("employees", loadEmployees);
  }
  if (tabName === "employees-attendance") {
    await safeLoad("employees", loadEmployees);
  }
  if (tabName === "attendance") {
    await safeLoad("employees", loadEmployees);
    await safeLoad("attendance", loadAttendance);
  }
  if (tabName === "attendance-settings") {
    if (initCache.attendanceRule) {
      applyAttendanceRule(initCache.attendanceRule);
    } else {
      await safeLoad("attendance-rules", loadAttendanceRule);
    }
  }
  if (tabName === "salary") {
    await safeLoad("employees", loadEmployees);
    await safeLoad("salary", loadSalary);
  }
  if (tabName === "org") {
    if (initCache.departments && initCache.positions && initCache.grades) {
      applyDepartments(initCache.departments, initCache.departmentTree || []);
      applyPositions(initCache.positions);
      applyGrades(initCache.grades);
    } else {
      await safeLoad("departments", loadDepartments);
      await safeLoad("positions", loadPositions);
      await safeLoad("grades", loadGrades);
    }
  }
  if (tabName === "permissions") {
    if (initCache.roles) {
      applyRoles(initCache.roles);
    } else {
      await safeLoad("roles", loadRoles);
    }
    if (initCache.permissions) {
      applyPermissions(initCache.permissions);
    } else {
      await safeLoad("permissions", loadPermissions);
    }
  }
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
    persistToken(result.token);
    $("auth-panel").classList.add("hidden");
    $("workspace").classList.remove("hidden");
    switchTab("dashboard");
    await safeLoad("init", loadInit);
    await ensureTabData("dashboard");
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
  persistToken(null);
  $("workspace").classList.add("hidden");
  $("auth-panel").classList.remove("hidden");
});

// Auto-login if token exists
window.addEventListener("DOMContentLoaded", async () => {
  if (authToken) {
    // 验证 Token 是否有效
    try {
      const response = await fetch(`${API_BASE}/dashboard`, {
        headers: {
          Authorization: `Bearer ${authToken}`
        }
      });
      
      if (!response.ok) {
        // Token 无效，清除并显示登录界面
        persistToken(null);
        $("auth-panel").classList.remove("hidden");
        $("workspace").classList.add("hidden");
        return;
      }
      
      // Token 有效，进入工作区
      $("auth-panel").classList.add("hidden");
      $("workspace").classList.remove("hidden");
      switchTab("dashboard");
      await safeLoad("init", loadInit);
      await ensureTabData("dashboard");
    } catch (err) {
      console.error("自动登录失败:", err);
      persistToken(null);
      $("auth-panel").classList.remove("hidden");
      $("workspace").classList.add("hidden");
    }
  }
});

// Employee form
$("employee-form").addEventListener("submit", async (e) => {
  e.preventDefault();
  const data = Object.fromEntries(new FormData(e.target));
  const deptSelect = $("employee-dept");
  const posSelect = $("employee-pos");
  const gradeSelect = $("employee-grade");
  data.departmentId = deptSelect ? Number(deptSelect.value) : null;
  data.positionId = posSelect ? Number(posSelect.value) : null;
  data.gradeId = gradeSelect ? Number(gradeSelect.value) : null;
  data.managerId = data.managerId ? Number(data.managerId) : null;
  // Sync legacy text fields for compatibility
  data.department = deptSelect ? deptSelect.options[deptSelect.selectedIndex].textContent : data.department;
  data.title = posSelect ? posSelect.options[posSelect.selectedIndex].textContent : data.title;
  data.status = "在职";
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
    if (!employeeId) {
      alert("请选择员工");
      return;
    }
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
    const similarity =
      typeof data.similarity === "number" ? `${data.similarity.toFixed(1)}%` : "未知";
    const algo = data.algorithm || "unknown";
    $("profile-result").textContent = data.matched
      ? `验证通过，相似度 ${similarity}（${algo}）`
      : `验证失败，相似度 ${similarity}（${algo}）`;
  });
}

const profileCheckinBtn = $("profile-checkin");
if (profileCheckinBtn) {
  profileCheckinBtn.addEventListener("click", async () => {
    const employeeId = $("profile-employee").value;
    if (!employeeId) {
      alert("请选择员工");
      return;
    }
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
      $("profile-result").textContent = `签到失败：${err}`;
      return;
    }
    const data = await response.json();
    $("profile-result").textContent = `签到成功：${data.workDate} ${data.checkIn || ""}`;
    await loadAttendance();
  });
}

const profileCheckoutBtn = $("profile-checkout");
if (profileCheckoutBtn) {
  profileCheckoutBtn.addEventListener("click", async () => {
    const employeeId = $("profile-employee").value;
    if (!employeeId) {
      alert("请选择员工");
      return;
    }
    const file = $("profile-verify").files[0];
    if (!file) {
      alert("请先选择识别文件");
      return;
    }
    const form = new FormData();
    form.append("employeeId", employeeId);
    form.append("file", file);
    const response = await fetch("/api/face/checkout", {
      method: "POST",
      headers: authToken ? { Authorization: `Bearer ${authToken}` } : {},
      body: form,
    });
    if (!response.ok) {
      const err = await response.text();
      $("profile-result").textContent = `签退失败：${err}`;
      return;
    }
    const data = await response.json();
    $("profile-result").textContent = `签退成功：${data.workDate} ${data.checkOut || ""}`;
    await loadAttendance();
  });
}

// Attendance form
$("attendance-form").addEventListener("submit", async (e) => {
  e.preventDefault();
  const data = Object.fromEntries(new FormData(e.target));
  data.employeeId = Number(data.employeeId);
  data.checkIn = normalizeEmptyToNull(data.checkIn);
  data.checkOut = normalizeEmptyToNull(data.checkOut);
  data.note = normalizeEmptyToNull(data.note);
  const error = validateAttendancePayload(data);
  if (error) {
    alert(error);
    return;
  }
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
  data.salaryMonth = normalizeSalaryMonth(data.salaryMonth);
  data.baseSalary = Number(data.baseSalary);
  data.bonus = Number(data.bonus);
  data.deduction = Number(data.deduction);
  const error = validateSalaryPayload(data);
  if (error) {
    alert(error);
    return;
  }
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
    await ensureTabData(btn.dataset.tab);
  });
});

// Report refresh

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
    const body = {
      workStartTime: $("rule-start-time").value || "09:00",
      workEndTime: $("rule-end-time").value || "18:00",
      lunchBreakStart: $("rule-lunch-start").value || "12:00",
      lunchBreakEnd: $("rule-lunch-end").value || "13:00",
      lateGraceMinutes: late,
      earlyLeaveGraceMinutes: Number($("rule-early-leave").value) || 10,
      absentThresholdMinutes: Number($("rule-absent").value) || 240,
      overtimeThresholdMinutes: overtime,
      requireOvertimeApproval: $("rule-ot-approval").checked,
    };
    const result = await apiRequest("/attendance-rules", {
      method: "PUT",
      body: JSON.stringify(body),
    });
    $("rule-result").textContent = `规则已保存：上班 ${result.workStartTime}-${result.workEndTime}，迟到宽限 ${result.lateGraceMinutes} 分钟，早退宽限 ${result.earlyLeaveGraceMinutes} 分钟，加班起算 ${result.overtimeThresholdMinutes} 分钟`;
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
    const type = $("data-type-select").value;
    const format = $("data-format-select").value;
    const fileInput = $("data-file-input");
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
    const type = $("data-type-select").value;
    const format = $("data-format-select").value;
    const url = `/data/export/${type}?format=${format}`;
    
    // 检查是否已登录
    if (!authToken) {
      alert("请先登录");
      $("auth-panel").classList.remove("hidden");
      $("workspace").classList.add("hidden");
      return;
    }
    
    try {
      const headers = {};
      if (authToken) {
        headers.Authorization = `Bearer ${authToken}`;
      }
      
      const response = await fetch(`${API_BASE}${url}`, { headers });
      
      if (!response.ok) {
        if (response.status === 403) {
          alert("认证已过期，请重新登录");
          persistToken(null);
          $("auth-panel").classList.remove("hidden");
          $("workspace").classList.add("hidden");
          return;
        }
        const error = await response.text();
        throw new Error(error || "导出失败");
      }
      
      const blob = await response.blob();
      const filename = `${type}.${format}`;
      
      // 创建下载链接
      const downloadUrl = window.URL.createObjectURL(blob);
      const a = document.createElement("a");
      a.href = downloadUrl;
      a.download = filename;
      document.body.appendChild(a);
      a.click();
      document.body.removeChild(a);
      window.URL.revokeObjectURL(downloadUrl);
    } catch (err) {
      console.error("导出失败:", err);
      alert(`导出失败：${err.message}`);
    }
  });
}

// Org management
const deptAddBtn = $("dept-add");
if (deptAddBtn) {
  deptAddBtn.addEventListener("click", async () => {
    const name = $("dept-name-input").value.trim();
    const parentId = $("dept-parent-select").value;
    if (!name) {
      alert("请输入部门名称");
      return;
    }
    await apiRequest("/departments", {
      method: "POST",
      body: JSON.stringify({ name, parentId: parentId ? Number(parentId) : null }),
    });
    $("dept-name-input").value = "";
    await loadDepartments();
  });
}

const posAddBtn = $("pos-add");
if (posAddBtn) {
  posAddBtn.addEventListener("click", async () => {
    const name = $("pos-name-input").value.trim();
    if (!name) {
      alert("请输入岗位名称");
      return;
    }
    await apiRequest("/positions", {
      method: "POST",
      body: JSON.stringify({ name }),
    });
    $("pos-name-input").value = "";
    await loadPositions();
  });
}

const gradeAddBtn = $("grade-add");
if (gradeAddBtn) {
  gradeAddBtn.addEventListener("click", async () => {
    const name = $("grade-name-input").value.trim();
    const level = Number($("grade-level-input").value);
    if (!name || Number.isNaN(level)) {
      alert("请输入职级名称和等级");
      return;
    }
    await apiRequest("/grades", {
      method: "POST",
      body: JSON.stringify({ name, level }),
    });
    $("grade-name-input").value = "";
    $("grade-level-input").value = "";
    await loadGrades();
  });
}

// Permissions form
const permAddBtn = $("perm-add");
if (permAddBtn) {
  permAddBtn.addEventListener("click", async () => {
    try {
      const role = $("perm-role-select").value;
      const method = $("perm-method-select").value;
      const pathPrefix = $("perm-path-input").value.trim();
      if (!pathPrefix) {
        alert("请输入路径前缀");
        return;
      }
      await apiRequest("/permissions", {
        method: "POST",
        body: JSON.stringify({ role, method, pathPrefix }),
      });
      $("perm-path-input").value = "";
      await loadPermissions();
    } catch (err) {
      alert(`新增失败: ${err.message}`);
    }
  });
}
