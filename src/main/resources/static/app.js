let initCache = {
  dashboard: null,
  attendanceRule: null,
  organizations: null,
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
  applyOrganizations(init.organizations);
  applyRoles(init.roles);
  applyPermissions(init.permissions);
}

window.addEventListener("DOMContentLoaded", async () => {
  console.log("[热重载测试] 页面加载成功 - " + new Date().toLocaleTimeString());
  initAuth();
  initTabs();
  await activateTab("dashboard");
});
