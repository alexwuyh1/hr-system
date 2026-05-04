let selectedRole = null;
let selectedRoleMode = null;

async function loadRoles() {
  const roles = await apiRequest(API.permissions.roles);
  initCache.roles = roles;
  applyRoles(roles);
}

function applyRoles(roles) {
  if (!roles) return;
  const list = $("role-list");
  if (!list) return;
  list.innerHTML = "";
  roles.forEach((r) => {
    const li = document.createElement("li");
    const modeLabel = r.roleMode === "blacklist" ? "黑" : "白";
    const modeClass = r.roleMode === "blacklist" ? "mode-blacklist" : "mode-whitelist";
    li.innerHTML = `${r.name} <span class="${modeClass}">${modeLabel}</span>`;
    li.className = selectedRole === r.role ? "active" : "";
    li.onclick = () => selectRole(r.role, r.roleMode);
    list.appendChild(li);
  });
}

function selectRole(role, roleMode) {
  selectedRole = role;
  selectedRoleMode = roleMode;
  $("selected-role-name").textContent = role;
  const modeTag = $("selected-role-mode");
  modeTag.textContent = roleMode === "blacklist" ? "黑名单模式" : "白名单模式";
  modeTag.className = "role-mode-tag " + (roleMode === "blacklist" ? "tag-blacklist" : "tag-whitelist");
  $("perm-add-btn").disabled = false;
  applyRoles(initCache.roles);
  const perms = (initCache.permissions || []).filter(p => p.role === role);
  renderPermissions(perms);
}

function getModeLabel(roleMode) {
  return roleMode === "blacklist" ? "禁止" : "允许";
}

function renderPermissions(perms) {
  const body = $("perm-table").querySelector("tbody");
  body.innerHTML = "";
  const modeLabel = getModeLabel(selectedRoleMode);
  perms.forEach((p) => {
    const row = document.createElement("tr");
    const td1 = document.createElement("td");
    td1.textContent = p.method;
    row.appendChild(td1);
    const td2 = document.createElement("td");
    td2.textContent = p.pathPrefix;
    row.appendChild(td2);
    const td3 = document.createElement("td");
    const modeClass = selectedRoleMode === "blacklist" ? "mode-deny" : "mode-allow";
    td3.innerHTML = `<span class="${modeClass}">${modeLabel}</span>`;
    row.appendChild(td3);
    const td4 = document.createElement("td");
    const deleteBtn = document.createElement("button");
    deleteBtn.textContent = "删除";
    deleteBtn.className = "ghost";
    deleteBtn.onclick = async () => {
      await apiRequest(API.permissions.delete(p.id), { method: "DELETE" });
      await safeLoad("permissions", loadPermissions);
    };
    td4.appendChild(deleteBtn);
    row.appendChild(td4);
    body.appendChild(row);
  });
}

function confirmDeleteRole(name) {
  showConfirm(`确定要删除角色"${name}"吗？这将同时删除该角色的所有权限规则。`, async () => {
    await apiRequest(`/permissions/role/${encodeURIComponent(name)}`, { method: "DELETE" });
    selectedRole = null;
    selectedRoleMode = null;
    $("selected-role-name").textContent = "请选择角色";
    $("selected-role-mode").textContent = "";
    $("perm-add-btn").disabled = true;
    $("perm-table").querySelector("tbody").innerHTML = "";
    await safeLoad("roles", loadRoles);
    await safeLoad("permissions", loadPermissions);
  });
}

function initPermissionTab() {
  const roleAddBtn = $("role-add-btn");
  if (roleAddBtn) {
    roleAddBtn.onclick = () => openRoleModal();
  }

  const permAddBtn = $("perm-add-btn");
  if (permAddBtn) {
    permAddBtn.onclick = () => openPermissionModal();
  }
}

function openRoleModal() {
  openModal(
    '新增角色',
    `
      <form id="modal-role-form">
        <label>角色名称 <input id="modal-role-name" required placeholder="如：财务"></label>
        <label>权限模式 <select id="modal-role-mode" required>
          <option value="whitelist">白名单（仅允许的权限可访问）</option>
          <option value="blacklist">黑名单（默认全部可访问，禁止指定权限）</option>
        </select></label>
      </form>
    `,
    async () => {
      const name = $("modal-role-name").value.trim();
      const roleMode = $("modal-role-mode").value;
      if (!name) {
        alert("请输入角色名称");
        return;
      }
      const roles = initCache.roles || [];
      if (roles.some(r => r.role === name)) {
        alert("角色名称已存在");
        return;
      }
      await apiRequest(API.permissions.createRole, {
        method: "POST",
        body: JSON.stringify({ role: name, roleMode }),
      });
      await loadRoles();
      await safeLoad("permissions", loadPermissions);
    },
    { submitText: '保存' }
  );
}

async function loadPermissions() {
  const list = await apiRequest(API.permissions.list);
  initCache.permissions = list;
  if (selectedRole) {
    const perms = list.filter(p => p.role === selectedRole);
    renderPermissions(perms);
  }
}

function openPermissionModal() {
  const modeLabel = getModeLabel(selectedRoleMode);
  openModal(
    '新增权限',
    `
      <form id="modal-permission-form">
        <label>方法 <select id="modal-perm-method" required>
          <option value="GET">GET</option>
          <option value="POST">POST</option>
          <option value="PUT">PUT</option>
          <option value="DELETE">DELETE</option>
        </select></label>
        <label>路径 <input id="modal-perm-path" required placeholder="/api/employees"></label>
        <p class="perm-mode-hint">当前角色为${selectedRoleMode === "blacklist" ? "黑名单" : "白名单"}模式，此权限将标记为"<strong>${modeLabel}</strong>"</p>
      </form>
    `,
    async () => {
      const method = $("modal-perm-method").value;
      const pathPrefix = $("modal-perm-path").value.trim();
      if (!pathPrefix) {
        alert("请输入路径前缀");
        return;
      }
      await apiRequest(API.permissions.create, {
        method: "POST",
        body: JSON.stringify({ role: selectedRole, method, pathPrefix }),
      });
      await safeLoad("permissions", loadPermissions);
    },
    { submitText: '保存' }
  );
}
