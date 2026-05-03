async function loadRoles() {
  const roles = await apiRequest(API.permissions.roles);
  initCache.roles = roles;
  applyRoles(roles);
}

function applyRoles(roles) {
  if (!roles) return;
  const select = $("perm-role-select");
  if (!select) return;
  select.innerHTML = "";
  roles.forEach((r) => {
    const option = document.createElement("option");
    option.value = r;
    option.textContent = r;
    select.appendChild(option);
  });

  const roleTable = $("role-table");
  if (roleTable) {
    const body = roleTable.querySelector("tbody");
    body.innerHTML = "";
    roles.forEach((r) => {
      const tr = document.createElement("tr");
      const td1 = document.createElement("td");
      td1.textContent = r;
      tr.appendChild(td1);
      const td2 = document.createElement("td");
      const deleteBtn = document.createElement("button");
      deleteBtn.textContent = "删除";
      deleteBtn.className = "ghost";
      deleteBtn.onclick = () => confirmDeleteRole(r);
      td2.appendChild(deleteBtn);
      tr.appendChild(td2);
      body.appendChild(tr);
    });
  }
}

function confirmDeleteRole(name) {
  showConfirm(`确定要删除角色"${name}"吗？这将同时删除该角色的所有权限规则。`, async () => {
    const perms = initCache.permissions || [];
    const toDelete = perms.filter(p => p.role === name);
    for (const p of toDelete) {
      await apiRequest(API.permissions.delete(p.id), { method: "DELETE" });
    }
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
        body: JSON.stringify({ role: name, method: "GET", pathPrefix: "/api/dashboard" }),
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
  applyPermissions(list);
}

function applyPermissions(list) {
  if (!list) return;
  const body = $("perm-table").querySelector("tbody");
  body.innerHTML = "";
  list.forEach((p) => {
    const row = buildRow([p.role, p.method, p.pathPrefix], async () => {
      await apiRequest(API.permissions.delete(p.id), { method: "DELETE" });
      await safeLoad("permissions", loadPermissions);
    });
    body.appendChild(row);
  });
}

function openPermissionModal() {
  const roleOptions = (initCache.roles || [])
    .map(r => `<option value="${r}">${r}</option>`)
    .join('');

  openModal(
    '新增权限',
    `
      <form id="modal-permission-form">
        <label>角色 <select id="modal-perm-role" required><option value="">请选择</option>${roleOptions}</select></label>
        <label>方法 <select id="modal-perm-method" required>
          <option value="GET">GET</option>
          <option value="POST">POST</option>
          <option value="PUT">PUT</option>
          <option value="DELETE">DELETE</option>
        </select></label>
        <label>路径 <input id="modal-perm-path" required placeholder="/api/employees"></label>
      </form>
    `,
    async () => {
      const role = $("modal-perm-role").value;
      const method = $("modal-perm-method").value;
      const pathPrefix = $("modal-perm-path").value.trim();
      if (!pathPrefix) {
        alert("请输入路径前缀");
        return;
      }
      await apiRequest(API.permissions.create, {
        method: "POST",
        body: JSON.stringify({ role, method, pathPrefix }),
      });
      await loadPermissions();
    },
    { submitText: '保存' }
  );
}
