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

async function loadInit() {
  const init = await apiRequest(API.init);
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

window.addEventListener("DOMContentLoaded", async () => {
  initAuth();
  initTabs();
  await activateTab("dashboard");
});
