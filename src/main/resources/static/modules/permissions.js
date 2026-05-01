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

function initPermissionForm() {
  const permAddBtn = $("perm-add");
  if (permAddBtn) {
    permAddBtn.addEventListener("click", async () => {
      try {
        const role = $("perm-role-select").value;
        const method = $("perm-method-select").value;
        const pathPrefix = $("perm-path-input").value.trim();
        if (!pathPrefix) {
          alert("请输入路径前缀");
          return;
        }
        await apiRequest(API.permissions.create, {
          method: "POST",
          body: JSON.stringify({ role, method, pathPrefix }),
        });
        $("perm-path-input").value = "";
        await loadPermissions();
      } catch (err) {
        alert(`新增失败: ${err.message}`);
      }
    });
  }
}
