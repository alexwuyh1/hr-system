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
    const row = buildRow(
      [e.employeeNo, e.name, e.departmentName || e.department, e.positionName || e.title, e.status],
      async () => {
        await apiRequest(API.employees.delete(e.id), { method: "DELETE" });
        await safeLoad("employees", loadEmployees);
      },
      [statusBtn]
    );
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

function initEmployeeForm() {
  $("employee-form").addEventListener("submit", async (e) => {
    e.preventDefault();
    const data = Object.fromEntries(new FormData(e.target));
    const deptSelect = $("employee-dept");
    const posSelect = $("employee-pos");
    const gradeSelect = $("employee-grade");
    data.departmentId = deptSelect ? Number(deptSelect.value) : null;
    data.positionId = posSelect ? Number(posSelect.value) : null;
    data.gradeId = gradeSelect ? Number(gradeSelect.value) : null;
    data.managerId = data.managerId ? Number(data.managerId) : null;
    data.department = deptSelect ? deptSelect.options[deptSelect.selectedIndex].textContent : data.department;
    data.title = posSelect ? posSelect.options[posSelect.selectedIndex].textContent : data.title;
    data.status = "在职";
    try {
      await apiRequest(API.employees.create, {
        method: "POST",
        body: JSON.stringify(data),
      });
      e.target.reset();
      await loadEmployees();
    } catch (err) {
      alert(`保存失败: ${err.message}`);
    }
  });
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
