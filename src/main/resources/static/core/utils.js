const $ = (id) => document.getElementById(id);

function setText(id, value) {
  const el = $(id);
  if (el) {
    el.textContent = value;
  }
}

function toMinutes(timeStr) {
  if (!timeStr) return null;
  const parts = timeStr.split(":");
  if (parts.length < 2) return null;
  const hours = Number(parts[0]);
  const minutes = Number(parts[1]);
  if (Number.isNaN(hours) || Number.isNaN(minutes)) return null;
  return hours * 60 + minutes;
}

function normalizeEmptyToNull(value) {
  return value === "" ? null : value;
}

function normalizeSalaryMonth(value) {
  if (!value) return value;
  if (value.length >= 7) {
    return value.slice(0, 7);
  }
  return value;
}

function validateAttendancePayload(data) {
  if (!data.employeeId || Number.isNaN(data.employeeId)) {
    return "请选择员工";
  }
  if (!data.workDate) {
    return "请选择日期";
  }
  if (!data.status) {
    return "请选择状态";
  }
  if (data.checkOut && !data.checkIn) {
    return "签退需要先填写签到时间";
  }
  const checkInMin = toMinutes(data.checkIn);
  const checkOutMin = toMinutes(data.checkOut);
  if (checkInMin != null && checkOutMin != null && checkOutMin < checkInMin) {
    return "签退时间不能早于签到时间";
  }
  return null;
}

function validateSalaryPayload(data) {
  if (!data.employeeId || Number.isNaN(data.employeeId)) {
    return "请选择员工";
  }
  if (!data.salaryMonth || !/^\d{4}-\d{2}$/.test(data.salaryMonth)) {
    return "薪资日期格式应为 YYYY-MM";
  }
  if ([data.baseSalary, data.bonus, data.deduction].some((v) => Number.isNaN(v))) {
    return "薪资金额需要填写数值";
  }
  if (data.baseSalary < 0 || data.bonus < 0 || data.deduction < 0) {
    return "薪资金额不能为负数";
  }
  if (data.baseSalary + data.bonus - data.deduction < 0) {
    return "薪资总额不能为负数";
  }
  return null;
}

async function safeLoad(label, fn) {
  try {
    return await fn();
  } catch (err) {
    console.warn(`[load:${label}]`, err);
    return null;
  }
}

function buildRow(cells, onDelete, extraActions = []) {
  const tr = document.createElement("tr");
  cells.forEach((text) => {
    const td = document.createElement("td");
    td.textContent = text;
    tr.appendChild(td);
  });
  const actionTd = document.createElement("td");
  extraActions.forEach((action) => actionTd.appendChild(action));
  const delBtn = document.createElement("button");
  delBtn.textContent = "删除";
  delBtn.className = "ghost";
  delBtn.onclick = onDelete;
  actionTd.appendChild(delBtn);
  tr.appendChild(actionTd);
  return tr;
}
