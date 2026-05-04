let activeOrgType = "position";

const ORG_TYPE_CONFIG = {
  position: { title: "岗位管理", type: "岗位", showTree: true },
  dept: { title: "部门管理", type: "部门", showTree: false },
  grade: { title: "职级管理", type: "职级", showTree: false },
};

const ORG_TABLE_HEADS = {
  position: ["名称", "部门", "职级"],
  dept: ["名称", "上级"],
  grade: ["名称", "等级"],
};

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

  const config = ORG_TYPE_CONFIG[activeOrgType];
  const items = list.filter(o => o.type === config.type);

  const treeContainer = $("org-tree-container");
  if (treeContainer) {
    treeContainer.innerHTML = "";
    if (config.showTree && positionTreeData) {
      treeContainer.appendChild(renderTree(positionTreeData));
    }
  }

  const head = $("org-table-head");
  if (head) {
    const ths = ORG_TABLE_HEADS[activeOrgType].map(h => `<th>${h}</th>`).join("") + "<th>操作</th>";
    head.innerHTML = `<tr>${ths}</tr>`;
  }

  const body = $("org-table").querySelector("tbody");
  body.innerHTML = "";
  items.forEach((o) => {
    const cells = [];
    cells.push(o.name);
    if (activeOrgType === "position") {
      cells.push(o.parent ? o.parent.name : "-");
      cells.push(o.grade ? o.grade.name : "-");
    } else if (activeOrgType === "dept") {
      cells.push(o.parent ? o.parent.name : "-");
    } else if (activeOrgType === "grade") {
      cells.push(o.level || "-");
    }

    const row = buildRow(cells, async () => {
      await apiRequest(API.organizations.delete(o.id), { method: "DELETE" });
      await loadOrganizations();
    });
    body.appendChild(row);
  });
}

function switchOrgType(type) {
  activeOrgType = type;
  const config = ORG_TYPE_CONFIG[type];
  $("org-type-title").textContent = config.title;

  document.querySelectorAll("#org-type-list li").forEach(li => {
    li.className = li.dataset.type === type ? "active" : "";
  });

  const list = initCache.organizations || [];
  const treeData = initCache.organizationTree || [];
  const positionTreeData = initCache.positionTree || [];
  applyOrganizations(list, treeData, positionTreeData);
}

function initOrgConfigTab() {
  const addBtn = $("org-add-btn");
  if (addBtn) {
    addBtn.onclick = () => {
      if (activeOrgType === "position") openPositionModal();
      else if (activeOrgType === "dept") openDeptModal();
      else openGradeModal();
    };
  }

  document.querySelectorAll("#org-type-list li").forEach(li => {
    li.onclick = () => switchOrgType(li.dataset.type);
  });

  switchOrgType("position");
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
        <label>所属部门 <select id="modal-position-dept" required>${deptOptions}</select></label>
        <label>职级 <select id="modal-position-grade" required>${gradeOptions}</select></label>
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
      if (!deptId) {
        alert("请选择所属部门");
        return;
      }
      if (!gradeId) {
        alert("请选择职级");
        return;
      }
      await apiRequest(API.organizations.create, {
        method: "POST",
        body: JSON.stringify({
          name,
          type: "岗位",
          parentId: Number(deptId),
          gradeId: Number(gradeId),
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
