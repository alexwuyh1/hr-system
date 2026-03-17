const API_BASE = "/api";

// Store auth token in memory for this demo.
let authToken = null;

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

async function loadReport() {
  const report = await apiRequest("/reports/summary");
  $("stat-total").textContent = report.totalEmployees;
  $("stat-attendance").textContent = report.attendanceToday;
  $("stat-payroll").textContent = report.totalPayroll.toFixed(2);
  $("report-total").textContent = report.totalEmployees;
  $("report-attendance").textContent = report.attendanceToday;
  $("report-payroll").textContent = report.totalPayroll.toFixed(2);
}

async function loadEmployees() {
  const list = await apiRequest("/employees");
  const body = $("employee-table").querySelector("tbody");
  body.innerHTML = "";
  list.forEach((e) => {
    const row = buildRow(
      [e.employeeNo, e.name, e.department, e.title, e.status],
      async () => {
        await apiRequest(`/employees/${e.id}`, { method: "DELETE" });
        await loadEmployees();
      }
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
    await Promise.all([loadEmployees(), loadAttendance(), loadSalary(), loadReport()]);
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
  btn.addEventListener("click", () => switchTab(btn.dataset.tab));
});

// Report refresh
$("refresh-report").addEventListener("click", async () => {
  await loadReport();
});
