let activeSubTab = "position";

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
  const treeData = await apiRequest(API.organizations.tree);
  initCache.organizationTree = treeData;
  const positionTreeData = await apiRequest(API.organizations.positionTree);
  initCache.positionTree = positionTreeData;
  applyOrganizations(list, treeData, positionTreeData);
}

function applyOrganizations(list, treeData, positionTreeData) {
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

  const depts = list.filter(o => o.type === "部门");
  const deptBody = $("dept-table").querySelector("tbody");
  deptBody.innerHTML = "";
  depts.forEach((o) => {
    const row = buildRow(
      [o.name, o.parent ? o.parent.name : "-"],
      async () => {
        await apiRequest(API.organizations.delete(o.id), { method: "DELETE" });
        await loadOrganizations();
      }
    );
    deptBody.appendChild(row);
  });

  const positionTreeContainer = $("position-tree");
  if (positionTreeContainer && positionTreeData) {
    positionTreeContainer.innerHTML = "";
    positionTreeContainer.appendChild(renderTree(positionTreeData));
  }

  const positions = list.filter(o => o.type === "岗位");
  const posBody = $("position-table").querySelector("tbody");
  posBody.innerHTML = "";
  positions.forEach((o) => {
    const row = buildRow(
      [o.name, o.parent ? o.parent.name : "-", o.grade ? o.grade.name : "-"],
      async () => {
        await apiRequest(API.organizations.delete(o.id), { method: "DELETE" });
        await loadOrganizations();
      }
    );
    posBody.appendChild(row);
  });

  const grades = list.filter(o => o.type === "职级").sort((a, b) => (a.level || 0) - (b.level || 0));
  const gradeBody = $("grade-table").querySelector("tbody");
  gradeBody.innerHTML = "";
  grades.forEach((o) => {
    const row = buildRow(
      [o.name, o.level || "-"],
      async () => {
        await apiRequest(API.organizations.delete(o.id), { method: "DELETE" });
        await loadOrganizations();
      }
    );
    gradeBody.appendChild(row);
  });
}

function initOrgConfigTab() {
  const addBtn = $("dept-add-btn");
  if (addBtn) {
    addBtn.onclick = () => openDeptModal();
  }
  const posAddBtn = $("position-add-btn");
  if (posAddBtn) {
    posAddBtn.onclick = () => openPositionModal();
  }
  const gradeAddBtn = $("grade-add-btn");
  if (gradeAddBtn) {
    gradeAddBtn.onclick = () => openGradeModal();
  }

  document.querySelectorAll("[data-subtab]").forEach(btn => {
    btn.onclick = () => {
      document.querySelectorAll("[data-subtab]").forEach(b => b.classList.remove("active"));
      document.querySelectorAll(".sub-content").forEach(c => c.classList.remove("active"));
      btn.classList.add("active");
      $(`subtab-${btn.dataset.subtab}`).classList.add("active");
      activeSubTab = btn.dataset.subtab;
    };
  });
}

function openDeptModal() {
  const deptOptions = (initCache.organizations || [])
    .filter(o => o.type === "部门")
    .map(o => `<option value="${o.id}">${o.name}</option>`)
    .join('');

  openModal(
    '新增部门',
    `
      <form id="modal-dept-form">
        <label>名称 <input id="modal-dept-name" required placeholder="输入部门名称"></label>
        <label>上级部门 <select id="modal-dept-parent"><option value="">无</option>${deptOptions}</select></label>
      </form>
    `,
    async () => {
      const name = $("modal-dept-name").value.trim();
      const parentId = $("modal-dept-parent").value;
      if (!name) {
        alert("请输入名称");
        return;
      }
      await apiRequest(API.organizations.create, {
        method: "POST",
        body: JSON.stringify({
          name,
          type: "部门",
          parentId: parentId ? Number(parentId) : null,
          level: null
        }),
      });
      await loadOrganizations();
    },
    { submitText: '保存' }
  );
}

function openPositionModal() {
  const deptOptions = (initCache.organizations || [])
    .filter(o => o.type === "部门")
    .map(o => `<option value="${o.id}">${o.name}</option>`)
    .join('');

  const gradeOptions = (initCache.organizations || [])
    .filter(o => o.type === "职级")
    .sort((a, b) => (a.level || 0) - (b.level || 0))
    .map(o => `<option value="${o.id}">${o.name}</option>`)
    .join('');

  openModal(
    '新增岗位',
    `
      <form id="modal-position-form">
        <label>名称 <input id="modal-position-name" required placeholder="输入岗位名称"></label>
        <label>所属部门 <select id="modal-position-dept"><option value="">无</option>${deptOptions}</select></label>
        <label>职级 <select id="modal-position-grade"><option value="">无</option>${gradeOptions}</select></label>
      </form>
    `,
    async () => {
      const name = $("modal-position-name").value.trim();
      const deptId = $("modal-position-dept").value;
      const gradeId = $("modal-position-grade").value;
      if (!name) {
        alert("请输入名称");
        return;
      }
      await apiRequest(API.organizations.create, {
        method: "POST",
        body: JSON.stringify({
          name,
          type: "岗位",
          parentId: deptId ? Number(deptId) : null,
          gradeId: gradeId ? Number(gradeId) : null,
          level: null
        }),
      });
      await loadOrganizations();
    },
    { submitText: '保存' }
  );
}

function openGradeModal() {
  openModal(
    '新增职级',
    `
      <form id="modal-grade-form">
        <label>名称 <input id="modal-grade-name" required placeholder="输入职级名称"></label>
        <label>等级 <input id="modal-grade-level" type="number" required placeholder="职级等级数值"></label>
      </form>
    `,
    async () => {
      const name = $("modal-grade-name").value.trim();
      const level = $("modal-grade-level").value;
      if (!name) {
        alert("请输入名称");
        return;
      }
      if (!level) {
        alert("请输入等级");
        return;
      }
      await apiRequest(API.organizations.create, {
        method: "POST",
        body: JSON.stringify({
          name,
          type: "职级",
          parentId: null,
          level: Number(level)
        }),
      });
      await loadOrganizations();
    },
    { submitText: '保存' }
  );
}
