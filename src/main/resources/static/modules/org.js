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

async function loadOrganizations() {
  const list = await apiRequest(API.organizations.list);
  initCache.organizations = list;
  let treeData = initCache.organizationTree;
  if (!treeData) {
    treeData = await apiRequest(API.organizations.tree);
    initCache.organizationTree = treeData;
  }
  applyOrganizations(list, treeData);
}

function applyOrganizations(list, treeData) {
  if (!list) return;
  const select = $("employee-org");
  if (select) {
    select.innerHTML = "";
    list.filter(o => o.type === "部门").forEach((o) => {
      const option = document.createElement("option");
      option.value = o.id;
      option.textContent = o.name;
      select.appendChild(option);
    });
  }

  const body = $("org-table").querySelector("tbody");
  body.innerHTML = "";
  list.forEach((o) => {
    const row = buildRow(
      [o.name, o.type, o.parent ? o.parent.name : "-", o.level || "-"],
      async () => {
        await apiRequest(API.organizations.delete(o.id), { method: "DELETE" });
        await loadOrganizations();
      }
    );
    body.appendChild(row);
  });

  const treeContainer = $("org-tree");
  if (treeContainer && treeData) {
    treeContainer.innerHTML = "";
    treeContainer.appendChild(renderTree(treeData));
  }
}

function initOrgConfigTab() {
  const addBtn = $("org-add-btn");
  if (addBtn) {
    addBtn.onclick = () => openOrgModal();
  }
}

function openOrgModal() {
  const deptOptions = (initCache.organizations || [])
    .filter(o => o.type === "部门")
    .map(o => `<option value="${o.id}">${o.name}</option>`)
    .join('');

  openModal(
    '新增组织',
    `
      <form id="modal-org-form">
        <label>名称 <input id="modal-org-name" required placeholder="输入名称"></label>
        <label>类型 <select id="modal-org-type"><option value="部门">部门</option><option value="岗位">岗位</option><option value="职级">职级</option></select></label>
        <label>上级部门 <select id="modal-org-parent"><option value="">无</option>${deptOptions}</select></label>
        <label>等级 <input id="modal-org-level" type="number" placeholder="职级等级"></label>
      </form>
    `,
    async () => {
      const name = $("modal-org-name").value.trim();
      const type = $("modal-org-type").value;
      const parentId = $("modal-org-parent").value;
      const level = $("modal-org-level").value;
      if (!name) {
        alert("请输入名称");
        return;
      }
      await apiRequest(API.organizations.create, {
        method: "POST",
        body: JSON.stringify({
          name,
          type,
          parentId: parentId ? Number(parentId) : null,
          level: level ? Number(level) : null
        }),
      });
      await loadOrganizations();
    },
    { submitText: '保存' }
  );
}
