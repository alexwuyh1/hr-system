async function loadAttendance() {
  const list = await apiRequest(API.attendance.list);
  const body = $("attendance-table").querySelector("tbody");
  body.innerHTML = "";
  list.forEach((a) => {
    const editBtn = document.createElement("button");
    editBtn.textContent = "编辑";
    editBtn.className = "ghost";
    editBtn.onclick = () => openEditAttendanceModal(a);

    const deleteBtn = document.createElement("button");
    deleteBtn.textContent = "删除";
    deleteBtn.className = "ghost";
    deleteBtn.onclick = () => confirmDeleteAttendance(a.id);

    const row = document.createElement("tr");
    [
      a.employee?.name || a.employee?.employeeNo || a.employee?.id || "-",
      a.workDate,
      a.checkIn || "-",
      a.checkOut || "-",
      a.status,
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

function getAttendanceFormHTML(data = {}) {
  const employeeOptions = (initCache.employees || [])
    .filter(e => e.status === "在职")
    .map(e => 
      `<option value="${e.id}" ${data.employeeId === e.id ? 'selected' : ''}>${e.employeeNo} - ${e.name}</option>`
    ).join('');

  return `
    <form id="modal-attendance-form">
      <input type="hidden" name="id" value="${data.id || ''}">
      <label>员工 <select name="employeeId" required><option value="">请选择</option>${employeeOptions}</select></label>
      <label>日期 <input name="workDate" type="date" value="${data.workDate || ''}" required></label>
      <div class="form-grid-2">
        <label>签到时间 <input name="checkIn" type="time" value="${data.checkIn || ''}"></label>
        <label>签退时间 <input name="checkOut" type="time" value="${data.checkOut || ''}"></label>
      </div>
      <label>状态 <select name="status" required>
        <option value="Normal" ${data.status === 'Normal' ? 'selected' : ''}>Normal</option>
        <option value="Late" ${data.status === 'Late' ? 'selected' : ''}>Late</option>
        <option value="Absent" ${data.status === 'Absent' ? 'selected' : ''}>Absent</option>
        <option value="Leave" ${data.status === 'Leave' ? 'selected' : ''}>Leave</option>
      </select></label>
      <label>备注 <input name="note" value="${data.note || ''}" placeholder="备注（可选）"></label>
    </form>
  `;
}

function openCreateAttendanceModal() {
  openModal(
    '新增考勤记录',
    getAttendanceFormHTML(),
    async (formData) => {
      formData.employeeId = Number(formData.employeeId);
      formData.checkIn = normalizeEmptyToNull(formData.checkIn);
      formData.checkOut = normalizeEmptyToNull(formData.checkOut);
      formData.note = normalizeEmptyToNull(formData.note);
      const error = validateAttendancePayload(formData);
      if (error) {
        alert(error);
        return;
      }
      await apiRequest(API.attendance.create, {
        method: "POST",
        body: JSON.stringify(formData),
      });
      await loadAttendance();
    },
    { submitText: '保存' }
  );
}

function openEditAttendanceModal(record) {
  openModal(
    '编辑考勤记录',
    getAttendanceFormHTML(record),
    async (formData) => {
      formData.employeeId = Number(formData.employeeId);
      formData.checkIn = normalizeEmptyToNull(formData.checkIn);
      formData.checkOut = normalizeEmptyToNull(formData.checkOut);
      formData.note = normalizeEmptyToNull(formData.note);
      const error = validateAttendancePayload(formData);
      if (error) {
        alert(error);
        return;
      }
      await apiRequest(API.attendance.update(record.id), {
        method: "PUT",
        body: JSON.stringify(formData),
      });
      await loadAttendance();
    },
    { submitText: '保存' }
  );
}

function confirmDeleteAttendance(id) {
  showConfirm('确定要删除该考勤记录吗？', async () => {
    await apiRequest(API.attendance.delete(id), { method: "DELETE" });
    await loadAttendance();
  });
}

function initAttendanceTab() {
  const manualBtn = $("attendance-manual-btn");
  if (manualBtn) {
    manualBtn.onclick = () => openCreateAttendanceModal();
  }

  const rulesBtn = $("attendance-rules-btn");
  if (rulesBtn) {
    rulesBtn.onclick = () => openAttendanceRulesModal();
  }

  const faceCheckinBtn = $("attendance-face-checkin-btn");
  if (faceCheckinBtn) {
    faceCheckinBtn.onclick = () => openFaceCheckinModal();
  }

  const faceCheckoutBtn = $("attendance-face-checkout-btn");
  if (faceCheckoutBtn) {
    faceCheckoutBtn.onclick = () => openFaceCheckoutModal();
  }

  const faceVerifyBtn = $("attendance-face-verify-btn");
  if (faceVerifyBtn) {
    faceVerifyBtn.onclick = () => openFaceVerifyModal();
  }
}

function openAttendanceRulesModal() {
  const rule = initCache.attendanceRule || {};
  openModal(
    '考勤规则设置',
    `
      <form id="modal-attendance-rules-form">
        <h4>标准工时设置</h4>
        <div class="form-grid-2">
          <label>上班时间 <input id="modal-rule-start-time" type="time" value="${rule.workStartTime || '09:00'}"></label>
          <label>下班时间 <input id="modal-rule-end-time" type="time" value="${rule.workEndTime || '18:00'}"></label>
        </div>
        <div class="form-grid-2">
          <label>午休开始 <input id="modal-rule-lunch-start" type="time" value="${rule.lunchBreakStart || '12:00'}"></label>
          <label>午休结束 <input id="modal-rule-lunch-end" type="time" value="${rule.lunchBreakEnd || '13:00'}"></label>
        </div>
        <h4>考勤规则设置</h4>
        <div class="form-grid-2">
          <label>迟到宽限(分钟) <input id="modal-rule-late" type="number" value="${rule.lateGraceMinutes ?? 10}"></label>
          <label>早退宽限(分钟) <input id="modal-rule-early-leave" type="number" value="${rule.earlyLeaveGraceMinutes ?? 10}"></label>
        </div>
        <div class="form-grid-2">
          <label>旷工阈值(分钟) <input id="modal-rule-absent" type="number" value="${rule.absentThresholdMinutes ?? 240}"></label>
          <label>加班阈值(分钟) <input id="modal-rule-overtime" type="number" value="${rule.overtimeThresholdMinutes ?? 60}"></label>
        </div>
        <label>加班需审批 <input id="modal-rule-ot-approval" type="checkbox" ${rule.requireOvertimeApproval ? 'checked' : ''}></label>
        <h4>计算功能</h4>
        <div class="form-grid-2">
          <label>单日计算日期 <input id="modal-rule-date" type="date"></label>
          <label>区间计算开始 <input id="modal-rule-range-start" type="date"></label>
        </div>
        <label>区间计算结束 <input id="modal-rule-range-end" type="date"></label>
      </form>
    `,
    async () => {
      const body = {
        workStartTime: $("modal-rule-start-time").value || "09:00",
        workEndTime: $("modal-rule-end-time").value || "18:00",
        lunchBreakStart: $("modal-rule-lunch-start").value || "12:00",
        lunchBreakEnd: $("modal-rule-lunch-end").value || "13:00",
        lateGraceMinutes: Number($("modal-rule-late").value) || 10,
        earlyLeaveGraceMinutes: Number($("modal-rule-early-leave").value) || 10,
        absentThresholdMinutes: Number($("modal-rule-absent").value) || 240,
        overtimeThresholdMinutes: Number($("modal-rule-overtime").value) || 60,
        requireOvertimeApproval: $("modal-rule-ot-approval").checked,
      };
      await apiRequest(API.attendanceRules.update, {
        method: "PUT",
        body: JSON.stringify(body),
      });

      const date = $("modal-rule-date").value;
      if (date) {
        await apiRequest(API.attendanceRules.calculate(date), { method: "POST" });
      }

      const start = $("modal-rule-range-start").value;
      const end = $("modal-rule-range-end").value;
      if (start && end) {
        await apiRequest(API.attendanceRules.calculateRange(start, end), { method: "POST" });
      }

      await loadAttendance();
    },
    { submitText: '保存' }
  );
}

function openFaceCheckinModal() {
  const employeeOptions = (initCache.employees || [])
    .filter(e => e.status === "在职")
    .map(e => `<option value="${e.id}">${e.employeeNo} - ${e.name}</option>`)
    .join('');

  openModal(
    '人脸签到',
    `
      <form id="modal-face-checkin-form">
        <label>选择员工 <select id="modal-checkin-employee" required><option value="">请选择</option>${employeeOptions}</select></label>
        <label>识别文件 <input id="modal-checkin-file" type="file" accept="image/*" required></label>
      </form>
    `,
    async () => {
      const employeeId = $("modal-checkin-employee").value;
      const file = $("modal-checkin-file").files[0];
      if (!employeeId || !file) {
        alert("请选择员工和识别文件");
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
        throw new Error(err);
      }
      const data = await response.json();
      alert(`签到成功：${data.workDate} ${data.checkIn || ""}`);
      await loadAttendance();
    },
    { submitText: '签到' }
  );
}

function openFaceCheckoutModal() {
  const employeeOptions = (initCache.employees || [])
    .filter(e => e.status === "在职")
    .map(e => `<option value="${e.id}">${e.employeeNo} - ${e.name}</option>`)
    .join('');

  openModal(
    '人脸签退',
    `
      <form id="modal-face-checkout-form">
        <label>选择员工 <select id="modal-checkout-employee" required><option value="">请选择</option>${employeeOptions}</select></label>
        <label>识别文件 <input id="modal-checkout-file" type="file" accept="image/*" required></label>
      </form>
    `,
    async () => {
      const employeeId = $("modal-checkout-employee").value;
      const file = $("modal-checkout-file").files[0];
      if (!employeeId || !file) {
        alert("请选择员工和识别文件");
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
        throw new Error(err);
      }
      const data = await response.json();
      alert(`签退成功：${data.workDate} ${data.checkOut || ""}`);
      await loadAttendance();
    },
    { submitText: '签退' }
  );
}

function openFaceVerifyModal() {
  const employeeOptions = (initCache.employees || [])
    .filter(e => e.status === "在职")
    .map(e => `<option value="${e.id}">${e.employeeNo} - ${e.name}</option>`)
    .join('');

  openModal(
    '验证人脸',
    `
      <form id="modal-face-verify-form">
        <label>选择员工 <select id="modal-verify-employee" required><option value="">请选择</option>${employeeOptions}</select></label>
        <label>识别文件 <input id="modal-verify-file" type="file" accept="image/*" required></label>
      </form>
    `,
    async () => {
      const employeeId = $("modal-verify-employee").value;
      const file = $("modal-verify-file").files[0];
      if (!employeeId || !file) {
        alert("请选择员工和识别文件");
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
      const similarity = typeof data.similarity === "number" ? `${data.similarity.toFixed(1)}%` : "未知";
      const algo = data.algorithm || "unknown";
      alert(data.matched ? `验证通过，相似度 ${similarity}（${algo}）` : `验证失败，相似度 ${similarity}（${algo}）`);
    },
    { submitText: '验证' }
  );
}

function initAttendanceRules() {
  const ruleSaveBtn = $("rule-save");
  if (ruleSaveBtn) {
    ruleSaveBtn.addEventListener("click", async () => {
      const late = Number($("rule-late").value);
      const overtime = Number($("rule-overtime").value);
      if (Number.isNaN(late) || Number.isNaN(overtime)) {
        alert("请输入有效数字");
        return;
      }
      const body = {
        workStartTime: $("rule-start-time").value || "09:00",
        workEndTime: $("rule-end-time").value || "18:00",
        lunchBreakStart: $("rule-lunch-start").value || "12:00",
        lunchBreakEnd: $("rule-lunch-end").value || "13:00",
        lateGraceMinutes: late,
        earlyLeaveGraceMinutes: Number($("rule-early-leave").value) || 10,
        absentThresholdMinutes: Number($("rule-absent").value) || 240,
        overtimeThresholdMinutes: overtime,
        requireOvertimeApproval: $("rule-ot-approval").checked,
      };
      const result = await apiRequest(API.attendanceRules.update, {
        method: "PUT",
        body: JSON.stringify(body),
      });
      $("rule-result").textContent = `规则已保存：上班 ${result.workStartTime}-${result.workEndTime}，迟到宽限 ${result.lateGraceMinutes} 分钟，早退宽限 ${result.earlyLeaveGraceMinutes} 分钟，加班起算 ${result.overtimeThresholdMinutes} 分钟`;
    });
  }

  const ruleCalcBtn = $("rule-calc");
  if (ruleCalcBtn) {
    ruleCalcBtn.addEventListener("click", async () => {
      const date = $("rule-date").value;
      if (!date) {
        alert("请选择日期");
        return;
      }
      const result = await apiRequest(API.attendanceRules.calculate(date), { method: "POST" });
      $("rule-result").textContent = `已计算 ${date}，更新 ${result.updated} 条记录`;
      await loadAttendance();
    });
  }

  const ruleCalcRangeBtn = $("rule-calc-range");
  if (ruleCalcRangeBtn) {
    ruleCalcRangeBtn.addEventListener("click", async () => {
      const start = $("rule-start").value;
      const end = $("rule-end").value;
      if (!start || !end) {
        alert("请选择起止日期");
        return;
      }
      const result = await apiRequest(
        API.attendanceRules.calculateRange(start, end),
        { method: "POST" }
      );
      $("rule-result").textContent = `已计算 ${start} 到 ${end}，更新 ${result.updated} 条记录`;
      await loadAttendance();
    });
  }
}

async function loadAttendanceRule() {
  const rule = await apiRequest(API.attendanceRules.get);
  initCache.attendanceRule = rule;
  applyAttendanceRule(rule);
}

function applyAttendanceRule(rule) {
  if (!rule) return;
  const setVal = (id, val) => { const el = $(id); if (el) el.value = val ?? ""; };
  setVal("rule-start-time", rule.workStartTime);
  setVal("rule-end-time", rule.workEndTime);
  setVal("rule-lunch-start", rule.lunchBreakStart);
  setVal("rule-lunch-end", rule.lunchBreakEnd);
  setVal("rule-late", rule.lateGraceMinutes);
  setVal("rule-early-leave", rule.earlyLeaveGraceMinutes);
  setVal("rule-absent", rule.absentThresholdMinutes);
  setVal("rule-overtime", rule.overtimeThresholdMinutes);
  const approval = $("rule-ot-approval");
  if (approval) approval.checked = rule.requireOvertimeApproval === true;
}
