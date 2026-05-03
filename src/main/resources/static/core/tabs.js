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
      initEmployeeCenterTab();
      break;
    case "attendance":
      initAttendanceTab();
      break;
    case "salary":
      initSalaryTab();
      break;
    case "org-config":
      initOrgConfigTab();
      break;
    case "import-export":
      initImportExportTab();
      break;
    case "permissions":
      initPermissionTab();
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
    await safeLoad("employees", loadEmployeeCenter);
  }
  if (tabName === "attendance") {
    await safeLoad("employees", loadEmployees);
    await safeLoad("attendance", loadAttendance);
  }
  if (tabName === "salary") {
    await safeLoad("employees", loadEmployees);
    await safeLoad("salary", loadSalary);
  }
  if (tabName === "org-config") {
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

async function activateTab(tabName) {
  console.log("Activating tab:", tabName);
  switchTab(tabName);
  await ensureTabData(tabName);
  initTabEvents(tabName);
}

function initTabs() {
  const tabButtons = document.querySelectorAll(".tabs button[data-tab]");
  console.log("Found tab buttons:", tabButtons.length);
  tabButtons.forEach((btn) => {
    btn.addEventListener("click", async () => {
      console.log("Tab clicked:", btn.dataset.tab);
      await activateTab(btn.dataset.tab);
    });
  });
}
