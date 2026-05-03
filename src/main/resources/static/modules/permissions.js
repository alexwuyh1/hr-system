let selectedRole = null;

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
  const filtered = (roles || []).filter(r => r !== "管理员");
  filtered.forEach((r) => {
    const li = document.createElement("li");
    li.textContent = r;
    li.className = selectedRole === r ? "active" : "";
    li.onclick = () => selectRole(r);
    list.appendChild(li);
  });
}

function selectRole(role) {
  selectedRole = role;
  $("selected-role-name").textContent = role;
  $("perm-add-btn").disabled = false;
  applyRoles(initCache.roles);
  const perms = (initCache.permissions || []).filter(p => p.role === role);
  renderPermissions(perms);
}

function renderPermissions(perms) {
  const body = $("perm-table").querySelector("tbody");
  body.innerHTML = "";
  perms.forEach((p) => {
    const row = document.createElement("tr");
    const td1 = document.createElement("td");
    td1.textContent = p.method;
    row.appendChild(td1);
    const td2 = document.createElement("td");
    td2.textContent = p.pathPrefix;
    row.appendChild(td2);
    const td3 = document.createElement("td");
    const modeClass = p.mode === 'deny' ? 'mode-deny' : 'mode-allow';
    const modeText = p.mode === 'deny' ? '禁止' : '允许';
    td3.innerHTML = `<span class="${modeClass}">${modeText}</span>`;
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
    const perms = initCache.permissions || [];
    const toDelete = perms.filter(p => p.role === name);
    for (const p of toDelete) {
      await apiRequest(API.permissions.delete(p.id), { method: "DELETE" });
    }
    selectedRole = null;
    $("selected-role-name").textContent = "请选择角色";
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
      </form>
    `,
    async () => {
      const name = $("modal-role-name").value.trim();
      if (!name) {
        alert("请输入角色名称");
        return;
      }
      const roles = initCache.roles || [];
      if (roles.includes(name)) {
        alert("角色名称已存在");
        return;
      }
      await apiRequest(API.permissions.create, {
        method: "POST",
        body: JSON.stringify({ role: name, method: "GET", pathPrefix: "/api/dashboard", mode: "allow" }),
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
        <label>模式 <select id="modal-perm-mode" required>
          <option value="allow">允许</option>
          <option value="deny">禁止</option>
        </select></label>
      </form>
    `,
    async () => {
      const method = $("modal-perm-method").value;
      const pathPrefix = $("modal-perm-path").value.trim();
      const mode = $("modal-perm-mode").value;
      if (!pathPrefix) {
        alert("请输入路径前缀");
        return;
      }
      await apiRequest(API.permissions.create, {
        method: "POST",
        body: JSON.stringify({ role: selectedRole, method, pathPrefix, mode }),
      });
      await safeLoad("permissions", loadPermissions);
    },
    { submitText: '保存' }
  );
}
