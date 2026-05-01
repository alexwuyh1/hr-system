async function loadRoles() {
  const roles = await apiRequest(API.roles.list);
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

function initPermissionTab() {
  const permAddBtn = $("perm-add-btn");
  if (permAddBtn) {
    permAddBtn.onclick = () => openPermissionModal();
  }
}

function openPermissionModal() {
  const roleOptions = (initCache.roles || [])
    .map(r => `<option value="${r.name}">${r.name}</option>`)
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
