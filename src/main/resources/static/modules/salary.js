async function loadSalary() {
  const list = await apiRequest(API.salaries.list);
  const body = $("salary-table").querySelector("tbody");
  body.innerHTML = "";
  list.forEach((s) => {
    const row = buildRow(
      [
        s.employee?.name || s.employee?.employeeNo || s.employee?.id || "-",
        s.salaryMonth,
        s.baseSalary,
        s.bonus,
        s.deduction,
      ],
      async () => {
        await apiRequest(API.salaries.delete(s.id), { method: "DELETE" });
        await safeLoad("salary", loadSalary);
      }
    );
    body.appendChild(row);
  });
}

function initSalaryForm() {
  $("salary-form").addEventListener("submit", async (e) => {
    e.preventDefault();
    const data = Object.fromEntries(new FormData(e.target));
    data.employeeId = Number(data.employeeId);
    data.salaryMonth = normalizeSalaryMonth(data.salaryMonth);
    data.baseSalary = Number(data.baseSalary);
    data.bonus = Number(data.bonus);
    data.deduction = Number(data.deduction);
    const error = validateSalaryPayload(data);
    if (error) {
      alert(error);
      return;
    }
    try {
      await apiRequest(API.salaries.create, {
        method: "POST",
        body: JSON.stringify(data),
      });
      e.target.reset();
      await loadSalary();
    } catch (err) {
      alert(`保存失败: ${err.message}`);
    }
  });
}
