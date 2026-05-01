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

async function loadDepartments() {
  const list = await apiRequest(API.departments.list);
  initCache.departments = list;
  let treeData = initCache.departmentTree;
  if (!treeData) {
    treeData = await apiRequest(API.departments.tree);
    initCache.departmentTree = treeData;
  }
  applyDepartments(list, treeData);
}

function applyDepartments(list, treeData) {
  if (!list) return;
  const select = $("employee-dept");
  if (select) {
    select.innerHTML = "";
    list.forEach((d) => {
      const option = document.createElement("option");
      option.value = d.id;
      option.textContent = d.name;
      select.appendChild(option);
    });
  }

  const parentSelect = $("dept-parent-select");
  if (parentSelect) {
    parentSelect.innerHTML = "<option value=\"\">无</option>";
    list.forEach((d) => {
      const option = document.createElement("option");
      option.value = d.id;
      option.textContent = d.name;
      parentSelect.appendChild(option);
    });
  }

  const body = $("dept-table").querySelector("tbody");
  body.innerHTML = "";
  list.forEach((d) => {
    const row = buildRow(
      [d.name, d.parent ? d.parent.name : "-"],
      async () => {
        await apiRequest(API.departments.delete(d.id), { method: "DELETE" });
        await loadDepartments();
      }
    );
    body.appendChild(row);
  });

  const treeContainer = $("dept-tree");
  if (treeContainer && treeData) {
    treeContainer.innerHTML = "";
    treeContainer.appendChild(renderTree(treeData));
  }
}

async function loadPositions() {
  const list = await apiRequest(API.positions.list);
  initCache.positions = list;
  applyPositions(list);
}

function applyPositions(list) {
  if (!list) return;
  const select = $("employee-pos");
  if (select) {
    select.innerHTML = "";
    list.forEach((p) => {
      const option = document.createElement("option");
      option.value = p.id;
      option.textContent = p.name;
      select.appendChild(option);
    });
  }

  const body = $("pos-table").querySelector("tbody");
  body.innerHTML = "";
  list.forEach((p) => {
    const row = buildRow([p.name], async () => {
      await apiRequest(API.positions.delete(p.id), { method: "DELETE" });
      await loadPositions();
    });
    body.appendChild(row);
  });
}

async function loadGrades() {
  const list = await apiRequest(API.grades.list);
  initCache.grades = list;
  applyGrades(list);
}

function applyGrades(list) {
  if (!list) return;
  const select = $("employee-grade");
  if (select) {
    select.innerHTML = "";
    list.forEach((g) => {
      const option = document.createElement("option");
      option.value = g.id;
      option.textContent = `${g.name} (L${g.level})`;
      select.appendChild(option);
    });
  }

  const body = $("grade-table").querySelector("tbody");
  body.innerHTML = "";
  list.forEach((g) => {
    const row = buildRow([g.name, g.level], async () => {
      await apiRequest(API.grades.delete(g.id), { method: "DELETE" });
      await loadGrades();
    });
    body.appendChild(row);
  });
}

function initOrgConfigTab() {
  const deptAddBtn = $("org-dept-add-btn");
  if (deptAddBtn) {
    deptAddBtn.onclick = () => openDeptModal();
  }

  const gradeAddBtn = $("org-grade-add-btn");
  if (gradeAddBtn) {
    gradeAddBtn.onclick = () => openGradeModal();
  }

  const posAddBtn = $("org-pos-add-btn");
  if (posAddBtn) {
    posAddBtn.onclick = () => openPositionModal();
  }
}

function openDeptModal() {
  const parentOptions = (initCache.departments || [])
    .map(d => `<option value="${d.id}">${d.name}</option>`)
    .join('');

  openModal(
    '新增部门',
    `
      <form id="modal-dept-form">
        <label>部门名称 <input id="modal-dept-name" required placeholder="输入部门名称"></label>
        <label>上级部门 <select id="modal-dept-parent"><option value="">无</option>${parentOptions}</select></label>
      </form>
    `,
    async () => {
      const name = $("modal-dept-name").value.trim();
      const parentId = $("modal-dept-parent").value;
      if (!name) {
        alert("请输入部门名称");
        return;
      }
      await apiRequest(API.departments.create, {
        method: "POST",
        body: JSON.stringify({ name, parentId: parentId ? Number(parentId) : null }),
      });
      await loadDepartments();
    },
    { submitText: '保存' }
  );
}

function openGradeModal() {
  openModal(
    '新增职级',
    `
      <form id="modal-grade-form">
        <label>职级名称 <input id="modal-grade-name" required placeholder="输入职级名称"></label>
        <label>等级 <input id="modal-grade-level" type="number" required placeholder="输入等级"></label>
      </form>
    `,
    async () => {
      const name = $("modal-grade-name").value.trim();
      const level = Number($("modal-grade-level").value);
      if (!name || Number.isNaN(level)) {
        alert("请输入职级名称和等级");
        return;
      }
      await apiRequest(API.grades.create, {
        method: "POST",
        body: JSON.stringify({ name, level }),
      });
      await loadGrades();
    },
    { submitText: '保存' }
  );
}

function openPositionModal() {
  openModal(
    '新增岗位',
    `
      <form id="modal-position-form">
        <label>岗位名称 <input id="modal-position-name" required placeholder="输入岗位名称"></label>
      </form>
    `,
    async () => {
      const name = $("modal-position-name").value.trim();
      if (!name) {
        alert("请输入岗位名称");
        return;
      }
      await apiRequest(API.positions.create, {
        method: "POST",
        body: JSON.stringify({ name }),
      });
      await loadPositions();
    },
    { submitText: '保存' }
  );
}
