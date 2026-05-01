async function loadSalary() {
  const list = await apiRequest(API.salaries.list);
  const body = $("salary-table").querySelector("tbody");
  body.innerHTML = "";
  list.forEach((s) => {
    const editBtn = document.createElement("button");
    editBtn.textContent = "编辑";
    editBtn.className = "ghost";
    editBtn.onclick = () => openEditSalaryModal(s);

    const deleteBtn = document.createElement("button");
    deleteBtn.textContent = "删除";
    deleteBtn.className = "ghost";
    deleteBtn.onclick = () => confirmDeleteSalary(s.id);

    const row = document.createElement("tr");
    [
      s.employee?.name || s.employee?.employeeNo || s.employee?.id || "-",
      s.salaryMonth,
      s.baseSalary,
      s.bonus,
      s.deduction,
    ].forEach((text) => {
      const td = document.createElement("td");
      td.textContent = text;
      row.appendChild(td);
    });
    const actionTd = document.createElement("td");
    actionTd.appendChild(editBtn);
    actionTd.appendChild(deleteBtn);
    row.appendChild(actionTd);
    body.appendChild(row);
  });
}

function getSalaryFormHTML(data = {}) {
  const employeeOptions = (initCache.employees || [])
    .map(e => 
      `<option value="${e.id}" ${data.employeeId === e.id ? 'selected' : ''}>${e.employeeNo} - ${e.name}</option>`
    ).join('');

  return `
    <form id="modal-salary-form">
      <input type="hidden" name="id" value="${data.id || ''}">
      <label>员工 <select name="employeeId" required><option value="">请选择</option>${employeeOptions}</select></label>
      <label>薪资月份 <input name="salaryMonth" type="month" value="${data.salaryMonth || ''}" required></label>
      <div class="form-grid-3">
        <label>基本薪资 <input name="baseSalary" type="number" step="0.01" value="${data.baseSalary || ''}" placeholder="输入基本薪资" required></label>
        <label>奖金 <input name="bonus" type="number" step="0.01" value="${data.bonus || ''}" placeholder="输入奖金" required></label>
        <label>扣款 <input name="deduction" type="number" step="0.01" value="${data.deduction || ''}" placeholder="输入扣款" required></label>
      </div>
    </form>
  `;
}

function openCreateSalaryModal() {
  openModal(
    '新增薪资记录',
    getSalaryFormHTML(),
    async (formData) => {
      formData.employeeId = Number(formData.employeeId);
      formData.salaryMonth = normalizeSalaryMonth(formData.salaryMonth);
      formData.baseSalary = Number(formData.baseSalary);
      formData.bonus = Number(formData.bonus);
      formData.deduction = Number(formData.deduction);
      const error = validateSalaryPayload(formData);
      if (error) {
        alert(error);
        return;
      }
      await apiRequest(API.salaries.create, {
        method: "POST",
        body: JSON.stringify(formData),
      });
      await loadSalary();
    },
    { submitText: '保存' }
  );
}

function openEditSalaryModal(record) {
  openModal(
    '编辑薪资记录',
    getSalaryFormHTML(record),
    async (formData) => {
      formData.employeeId = Number(formData.employeeId);
      formData.salaryMonth = normalizeSalaryMonth(formData.salaryMonth);
      formData.baseSalary = Number(formData.baseSalary);
      formData.bonus = Number(formData.bonus);
      formData.deduction = Number(formData.deduction);
      const error = validateSalaryPayload(formData);
      if (error) {
        alert(error);
        return;
      }
      await apiRequest(API.salaries.update(record.id), {
        method: "PUT",
        body: JSON.stringify(formData),
      });
      await loadSalary();
    },
    { submitText: '保存' }
  );
}

function confirmDeleteSalary(id) {
  showConfirm('确定要删除该薪资记录吗？', async () => {
    await apiRequest(API.salaries.delete(id), { method: "DELETE" });
    await loadSalary();
  });
}

function initSalaryTab() {
  const addBtn = $("salary-add-btn");
  if (addBtn) {
    addBtn.onclick = () => openCreateSalaryModal();
  }
}
