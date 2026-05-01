const loadedTabs = new Set();
const initializedModules = new Set();

async function loadTabHTML(tabName) {
  const container = document.getElementById(`tab-${tabName}`);
  if (!container) return;
  if (container.innerHTML.trim()) return;
  try {
    const response = await fetch(`tabs/${tabName}.html`);
    if (response.ok) {
      container.innerHTML = await response.text();
    }
  } catch (e) {
    console.warn(`Failed to load tab HTML: ${tabName}`, e);
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

function initTabEvents(tabName) {
  if (initializedModules.has(tabName)) return;
  initializedModules.add(tabName);

  switch (tabName) {
    case "dashboard":
      initDashboard();
      break;
    case "employees":
      initEmployeeForm();
      break;
    case "employees-attendance":
      initProfileEvents();
      break;
    case "attendance":
      initAttendanceForm();
      break;
    case "attendance-settings":
      initAttendanceRules();
      break;
    case "salary":
      initSalaryForm();
      break;
    case "org":
      initOrgEvents();
      break;
    case "import":
      initImportExport();
      break;
    case "permissions":
      initPermissionForm();
      break;
  }
}

async function ensureTabData(tabName) {
  await loadTabHTML(tabName);

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
    await safeLoad("attendance-rules", loadAttendanceRule);
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
  if (tabName === "import") {
    await safeLoad("import-export", loadImportExport);
  }
}

async function activateTab(tabName) {
  switchTab(tabName);
  await ensureTabData(tabName);
  initTabEvents(tabName);
}

function initTabs() {
  document.querySelectorAll(".tabs button[data-tab]").forEach((btn) => {
    btn.addEventListener("click", async () => {
      await activateTab(btn.dataset.tab);
    });
  });
}
