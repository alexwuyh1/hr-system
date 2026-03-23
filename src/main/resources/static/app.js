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

async function loadReport() {
  const report = await apiRequest("/reports/summary");
  $("stat-total").textContent = report.totalEmployees;
  $("stat-attendance").textContent = report.attendanceToday;
  $("stat-payroll").textContent = report.totalPayroll.toFixed(2);
  $("report-total").textContent = report.totalEmployees;
  $("report-attendance").textContent = report.attendanceToday;
  $("report-payroll").textContent = report.totalPayroll.toFixed(2);
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
      loadEmployees(),
      loadAttendance(),
      loadSalary(),
      loadReport(),
      loadDepartments().catch(() => {}),
      loadPositions().catch(() => {}),
      loadGrades().catch(() => {}),
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
