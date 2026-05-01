async function loadEmployees() {
  const list = await apiRequest(API.employees.list);
  initCache.employees = list;
  applyEmployees(list);

  const profileSelect = $("profile-employee");
  if (profileSelect) {
    profileSelect.innerHTML = "";
    list.filter((e) => e.status === "在职").forEach((e) => {
      const option = document.createElement("option");
      option.value = e.id;
      option.textContent = `${e.employeeNo} - ${e.name}`;
      option.dataset.avatar = e.avatarUrl || "";
      option.dataset.info = JSON.stringify(e);
      profileSelect.appendChild(option);
    });
    if (profileSelect.options.length > 0) {
      renderProfile(JSON.parse(profileSelect.options[0].dataset.info));
    }
  }

  const attendanceSelect = $("attendance-employee");
  if (attendanceSelect) {
    attendanceSelect.innerHTML = "";
    list.filter((e) => e.status === "在职").forEach((e) => {
      const option = document.createElement("option");
      option.value = e.id;
      option.textContent = `${e.employeeNo} - ${e.name}`;
      attendanceSelect.appendChild(option);
    });
  }

  const salarySelect = $("salary-employee");
  if (salarySelect) {
    salarySelect.innerHTML = "";
    list.forEach((e) => {
      const option = document.createElement("option");
      option.value = e.id;
      option.textContent = `${e.employeeNo} - ${e.name}`;
      salarySelect.appendChild(option);
    });
  }
}

function applyEmployees(list) {
  if (!list) return;
  const body = $("employee-table").querySelector("tbody");
  body.innerHTML = "";
  list.forEach((e) => {
    const statusBtn = document.createElement("button");
    const isActive = e.status === "在职";
    statusBtn.textContent = isActive ? "离职" : "复职";
    statusBtn.className = "ghost";
    statusBtn.onclick = async () => {
      const endpoint = isActive ? API.employees.resign : API.employees.rehire;
      await apiRequest(endpoint, {
        method: "POST",
        body: JSON.stringify({ employeeNo: e.employeeNo }),
      });
      await safeLoad("employees", loadEmployees);
    };

    const editBtn = document.createElement("button");
    editBtn.textContent = "编辑";
    editBtn.className = "ghost";
    editBtn.onclick = () => openEditEmployeeModal(e);

    const deleteBtn = document.createElement("button");
    deleteBtn.textContent = "删除";
    deleteBtn.className = "ghost";
    deleteBtn.onclick = () => confirmDeleteEmployee(e.id);

    const row = document.createElement("tr");
    [e.employeeNo, e.name, e.departmentName || e.department, e.positionName || e.title, e.status].forEach((text) => {
      const td = document.createElement("td");
      td.textContent = text;
      row.appendChild(td);
    });
    const actionTd = document.createElement("td");
    actionTd.appendChild(editBtn);
    actionTd.appendChild(statusBtn);
    actionTd.appendChild(deleteBtn);
    row.appendChild(actionTd);
    body.appendChild(row);
  });
}

function renderProfile(employee) {
  const info = $("profile-info");
  if (!info) return;
  const avatar = $("profile-avatar-img");
  avatar.src = employee.avatarUrl || "";
  avatar.alt = employee.name || "avatar";
  info.innerHTML = `
    <div><strong>${employee.name}</strong> <span>(${employee.employeeNo})</span></div>
    <div>部门：${employee.departmentName || employee.department || "-"}</div>
    <div>岗位：${employee.positionName || employee.title || "-"}</div>
    <div>职级：${employee.gradeName || "-"}</div>
    <div>直属上级：${employee.managerName || "-"}</div>
    <div>邮箱：${employee.email || "-"}</div>
    <div>电话：${employee.phone || "-"}</div>
  `;
}

function getEmployeeFormHTML(data = {}) {
  const deptOptions = (initCache.departments || []).map(d => 
    `<option value="${d.id}" ${data.departmentId === d.id ? 'selected' : ''}>${d.name}</option>`
  ).join('');
  
  const posOptions = (initCache.positions || []).map(p => 
    `<option value="${p.id}" ${data.positionId === p.id ? 'selected' : ''}>${p.name}</option>`
  ).join('');
  
  const gradeOptions = (initCache.grades || []).map(g => 
    `<option value="${g.id}" ${data.gradeId === g.id ? 'selected' : ''}>${g.name}</option>`
  ).join('');
  
  const managerOptions = (initCache.employees || [])
    .filter(e => e.status === "在职" && e.id !== data.id)
    .map(e => 
      `<option value="${e.id}" ${data.managerId === e.id ? 'selected' : ''}>${e.employeeNo} - ${e.name}</option>`
    ).join('');

  return `
    <form id="modal-employee-form">
      <input type="hidden" name="id" value="${data.id || ''}">
      <div class="form-grid-2">
        <label>工号 <input name="employeeNo" value="${data.employeeNo || ''}" required></label>
        <label>姓名 <input name="name" value="${data.name || ''}" required></label>
      </div>
      <div class="form-grid-2">
        <label>部门 <select name="departmentId" required><option value="">请选择</option>${deptOptions}</select></label>
        <label>职位 <select name="positionId" required><option value="">请选择</option>${posOptions}</select></label>
      </div>
      <div class="form-grid-2">
        <label>职级 <select name="gradeId"><option value="">请选择</option>${gradeOptions}</select></label>
        <label>直属上级 <select name="managerId"><option value="">无</option>${managerOptions}</select></label>
      </div>
      <div class="form-grid-2">
        <label>入职日期 <input name="hireDate" type="date" value="${data.hireDate || ''}" required></label>
        <label>邮箱 <input name="email" type="email" value="${data.email || ''}"></label>
      </div>
      <div class="form-grid-2">
        <label>电话 <input name="phone" value="${data.phone || ''}"></label>
        <label></label>
      </div>
    </form>
  `;
}

function openCreateEmployeeModal() {
  openModal(
    '新增员工',
    getEmployeeFormHTML(),
    async (formData) => {
      formData.departmentId = Number(formData.departmentId);
      formData.positionId = Number(formData.positionId);
      formData.gradeId = formData.gradeId ? Number(formData.gradeId) : null;
      formData.managerId = formData.managerId ? Number(formData.managerId) : null;
      formData.status = "在职";
      
      const dept = (initCache.departments || []).find(d => d.id === formData.departmentId);
      const pos = (initCache.positions || []).find(p => p.id === formData.positionId);
      formData.department = dept ? dept.name : '';
      formData.title = pos ? pos.name : '';

      await apiRequest(API.employees.create, {
        method: "POST",
        body: JSON.stringify(formData),
      });
      await loadEmployees();
    },
    { submitText: '入职' }
  );
}

function openEditEmployeeModal(employee) {
  openModal(
    '编辑员工',
    getEmployeeFormHTML(employee),
    async (formData) => {
      formData.id = employee.id;
      formData.departmentId = Number(formData.departmentId);
      formData.positionId = Number(formData.positionId);
      formData.gradeId = formData.gradeId ? Number(formData.gradeId) : null;
      formData.managerId = formData.managerId ? Number(formData.managerId) : null;
      
      const dept = (initCache.departments || []).find(d => d.id === formData.departmentId);
      const pos = (initCache.positions || []).find(p => p.id === formData.positionId);
      formData.department = dept ? dept.name : '';
      formData.title = pos ? pos.name : '';

      await apiRequest(API.employees.update(employee.id), {
        method: "PUT",
        body: JSON.stringify(formData),
      });
      await loadEmployees();
    },
    { submitText: '保存' }
  );
}

function confirmDeleteEmployee(id) {
  showConfirm('确定要删除该员工吗？此操作不可恢复。', async () => {
    await apiRequest(API.employees.delete(id), { method: "DELETE" });
    await loadEmployees();
  });
}

function initEmployeeTab() {
  // 员工管理页仅展示表格，新增功能已移至员工中心
}

let selectedEmployee = null;

function applyEmployeesCenter(list) {
  if (!list) return;
  const table = $("employee-center-table");
  if (!table) return;
  const body = table.querySelector("tbody");
  body.innerHTML = "";
  list.forEach((e) => {
    const row = document.createElement("tr");
    [e.employeeNo, e.name, e.departmentName || e.department, e.positionName || e.title, e.status].forEach((text) => {
      const td = document.createElement("td");
      td.textContent = text;
      row.appendChild(td);
    });
    row.style.cursor = "pointer";
    row.onclick = () => selectEmployee(e);
    body.appendChild(row);
  });
}

function selectEmployee(employee) {
  selectedEmployee = employee;
  openModal(
    '员工详情',
    `
      <div class="profile-card">
        <div class="profile-avatar">
          <img id="modal-avatar-img" src="${employee.avatarUrl || ''}" alt="avatar" />
        </div>
        <div>
          <div class="profile-info">
            <div><strong>${employee.name}</strong> <span>(${employee.employeeNo})</span></div>
            <div>部门：${employee.departmentName || employee.department || "-"}</div>
            <div>岗位：${employee.positionName || employee.title || "-"}</div>
            <div>职级：${employee.gradeName || "-"}</div>
            <div>邮箱：${employee.email || "-"}</div>
            <div>电话：${employee.phone || "-"}</div>
          </div>
          <div class="profile-status">
            <span style="color: ${employee.status === '在职' ? '#16a34a' : '#dc2626'}; font-weight: 600;">${employee.status}</span>
          </div>
          <div class="panel-actions" style="margin-top: 16px;">
            <button id="modal-edit-btn">编辑</button>
            <button id="modal-status-btn" class="ghost">${employee.status === '在职' ? '离职' : '复职'}</button>
            <button id="modal-delete-btn" class="ghost">删除</button>
            <button id="modal-upload-btn" class="ghost">上传头像</button>
          </div>
        </div>
      </div>
    `,
    null,
    { submitText: null }
  );
  $("modal-edit-btn").onclick = () => { closeModal(); openEditEmployeeModal(employee); };
  $("modal-status-btn").onclick = async () => {
    const endpoint = employee.status === '在职' ? API.employees.resign : API.employees.rehire;
    await apiRequest(endpoint, { method: "POST", body: JSON.stringify({ employeeNo: employee.employeeNo }) });
    closeModal();
    await loadEmployeeCenter();
  };
  $("modal-delete-btn").onclick = () => { closeModal(); confirmDeleteEmployee(employee.id); };
  $("modal-upload-btn").onclick = () => { closeModal(); openAvatarUploadModal(employee.id); };
}

async function loadEmployeeCenter() {
  const list = await apiRequest(API.employees.list);
  initCache.employees = list;
  applyEmployeesCenter(list);
}

function initEmployeeCenterTab() {
  const addBtn = $("employee-center-add-btn");
  if (addBtn) {
    addBtn.onclick = () => openCreateEmployeeModal();
  }
}

function openAvatarUploadModal(employeeId) {
  openModal(
    '上传头像',
    `
      <form id="modal-avatar-form">
        <label>头像文件 <input id="modal-avatar-file" type="file" accept="image/*" required></label>
      </form>
    `,
    async () => {
      const file = $("modal-avatar-file").files[0];
      if (!file) {
        alert("请选择头像文件");
        return;
      }
      const form = new FormData();
      form.append("file", file);
      const response = await fetch(`${API_BASE}${API.employees.avatar(employeeId)}`, {
        method: "POST",
        headers: authToken ? { Authorization: `Bearer ${authToken}` } : {},
        body: form,
      });
      if (!response.ok) {
        const err = await response.text();
        throw new Error(err);
      }
      await loadEmployeeCenter();
    },
    { submitText: '上传' }
  );
}

function initProfileEvents() {
  const profileSelect = $("profile-employee");
  if (profileSelect) {
    profileSelect.addEventListener("change", () => {
      const option = profileSelect.options[profileSelect.selectedIndex];
      const employee = JSON.parse(option.dataset.info);
      renderProfile(employee);
    });
  }

  const profileUploadBtn = $("profile-upload");
  if (profileUploadBtn) {
    profileUploadBtn.addEventListener("click", async () => {
      const employeeId = $("profile-employee").value;
      const file = $("profile-avatar").files[0];
      if (!file) {
        alert("请选择头像文件");
        return;
      }
      const form = new FormData();
      form.append("file", file);
      const response = await fetch(`${API_BASE}${API.employees.avatar(employeeId)}`, {
        method: "POST",
        headers: authToken ? { Authorization: `Bearer ${authToken}` } : {},
        body: form,
      });
      if (!response.ok) {
        const err = await response.text();
        $("profile-result").textContent = `上传失败：${err}`;
        return;
      }
      $("profile-result").textContent = "头像上传成功";
      await loadEmployees();
      const selected = $("profile-employee").value;
      const option = Array.from($("profile-employee").options).find((o) => o.value === selected);
      if (option) {
        renderProfile(JSON.parse(option.dataset.info));
      }
    });
  }

  const profileVerifyBtn = $("profile-verify-btn");
  if (profileVerifyBtn) {
    profileVerifyBtn.addEventListener("click", async () => {
      const employeeId = $("profile-employee").value;
      if (!employeeId) {
        alert("请选择员工");
        return;
      }
      const file = $("profile-verify").files[0];
      if (!file) {
        alert("请选择识别文件");
        return;
      }
      const form = new FormData();
      form.append("employeeId", employeeId);
      form.append("file", file);
      const response = await fetch(`${API_BASE}/face/verify`, {
        method: "POST",
        headers: authToken ? { Authorization: `Bearer ${authToken}` } : {},
        body: form,
      });
      const data = await response.json();
      const similarity =
        typeof data.similarity === "number" ? `${data.similarity.toFixed(1)}%` : "未知";
      const algo = data.algorithm || "unknown";
      $("profile-result").textContent = data.matched
        ? `验证通过，相似度 ${similarity}（${algo}）`
        : `验证失败，相似度 ${similarity}（${algo}）`;
    });
  }

  const profileCheckinBtn = $("profile-checkin");
  if (profileCheckinBtn) {
    profileCheckinBtn.addEventListener("click", async () => {
      const employeeId = $("profile-employee").value;
      if (!employeeId) {
        alert("请选择员工");
        return;
      }
      const file = $("profile-verify").files[0];
      if (!file) {
        alert("请先选择识别文件");
        return;
      }
      const form = new FormData();
      form.append("employeeId", employeeId);
      form.append("file", file);
      const response = await fetch(`${API_BASE}/face/checkin`, {
        method: "POST",
        headers: authToken ? { Authorization: `Bearer ${authToken}` } : {},
        body: form,
      });
      if (!response.ok) {
        const err = await response.text();
        $("profile-result").textContent = `签到失败：${err}`;
        return;
      }
      const data = await response.json();
      $("profile-result").textContent = `签到成功：${data.workDate} ${data.checkIn || ""}`;
      await loadAttendance();
    });
  }

  const profileCheckoutBtn = $("profile-checkout");
  if (profileCheckoutBtn) {
    profileCheckoutBtn.addEventListener("click", async () => {
      const employeeId = $("profile-employee").value;
      if (!employeeId) {
        alert("请选择员工");
        return;
      }
      const file = $("profile-verify").files[0];
      if (!file) {
        alert("请先选择识别文件");
        return;
      }
      const form = new FormData();
      form.append("employeeId", employeeId);
      form.append("file", file);
      const response = await fetch(`${API_BASE}/face/checkout`, {
        method: "POST",
        headers: authToken ? { Authorization: `Bearer ${authToken}` } : {},
        body: form,
      });
      if (!response.ok) {
        const err = await response.text();
        $("profile-result").textContent = `签退失败：${err}`;
        return;
      }
      const data = await response.json();
      $("profile-result").textContent = `签退成功：${data.workDate} ${data.checkOut || ""}`;
      await loadAttendance();
    });
  }
}
