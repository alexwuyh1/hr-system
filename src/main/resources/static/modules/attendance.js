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
      <label>员工 <select name="employeeId" required title="仅显示在职员工"><option value="">请选择在职员工</option>${employeeOptions}</select></label>
      <label>日期 <input name="workDate" type="date" value="${data.workDate || ''}" required title="考勤日期"></label>
      <div class="form-grid-2">
        <label>签到时间 <input name="checkIn" type="time" value="${data.checkIn || ''}" title="可选，如 09:00"></label>
        <label>签退时间 <input name="checkOut" type="time" value="${data.checkOut || ''}" title="可选，如 18:00"></label>
      </div>
      <label>状态 <select name="status" required title="考勤状态">
        <option value="Normal" ${data.status === 'Normal' ? 'selected' : ''}>正常</option>
        <option value="Late" ${data.status === 'Late' ? 'selected' : ''}>迟到</option>
        <option value="Absent" ${data.status === 'Absent' ? 'selected' : ''}>缺勤</option>
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

  const faceBtn = $("attendance-face-btn");
  if (faceBtn) {
    faceBtn.onclick = () => openFaceAttendanceModal();
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
        <h4>考勤规则设置</h4>
        <div class="form-grid-2">
          <label>迟到宽限(分钟) <input id="modal-rule-late" type="number" value="${rule.lateGraceMinutes ?? 10}"></label>
          <label>旷工阈值(分钟) <input id="modal-rule-absent" type="number" value="${rule.absentThresholdMinutes ?? 240}"></label>
        </div>
        <label>加班起算(分钟) <input id="modal-rule-overtime" type="number" value="${rule.overtimeThresholdMinutes ?? 60}"></label>
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
        lateGraceMinutes: Number($("modal-rule-late").value) || 10,
        absentThresholdMinutes: Number($("modal-rule-absent").value) || 240,
        overtimeThresholdMinutes: Number($("modal-rule-overtime").value) || 60,
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

function openFaceAttendanceModal() {
  const employeeOptions = (initCache.employees || [])
    .filter(e => e.status === "在职")
    .map(e => `<option value="${e.id}">${e.employeeNo} - ${e.name}</option>`)
    .join('');

  openModal(
    '人脸打卡',
    `
      <form id="modal-face-attendance-form">
        <label>选择员工 <select id="modal-face-employee" required><option value="">请选择</option>${employeeOptions}</select></label>
        <label>打卡类型 <select id="modal-face-type" required>
          <option value="checkin">签到</option>
          <option value="checkout">签退</option>
        </select></label>
        <label>识别文件 <input id="modal-face-file" type="file" accept="image/*" required></label>
      </form>
    `,
    async () => {
      const employeeId = $("modal-face-employee").value;
      const type = $("modal-face-type").value;
      const file = $("modal-face-file").files[0];
      if (!employeeId || !type || !file) {
        alert("请选择员工、打卡类型和识别文件");
        return;
      }
      const form = new FormData();
      form.append("employeeId", employeeId);
      form.append("file", file);
      const endpoint = type === "checkin" ? "checkin" : "checkout";
      const response = await fetch(`${API_BASE}/face/${endpoint}`, {
        method: "POST",
        headers: authToken ? { Authorization: `Bearer ${authToken}` } : {},
        body: form,
      });
      if (!response.ok) {
        const err = await response.text();
        throw new Error(err);
      }
      const data = await response.json();
      const msg = type === "checkin"
        ? `签到成功：${data.workDate} ${data.checkIn || ""}`
        : `签退成功：${data.workDate} ${data.checkOut || ""}`;
      alert(msg);
      await loadAttendance();
    },
    { submitText: '打卡' }
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
        lateGraceMinutes: late,
        absentThresholdMinutes: Number($("rule-absent").value) || 240,
        overtimeThresholdMinutes: overtime,
      };
      const result = await apiRequest(API.attendanceRules.update, {
        method: "PUT",
        body: JSON.stringify(body),
      });
      $("rule-result").textContent = `规则已保存：上班 ${result.workStartTime}-${result.workEndTime}，迟到宽限 ${result.lateGraceMinutes} 分钟，加班起算 ${result.overtimeThresholdMinutes} 分钟`;
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
  setVal("rule-late", rule.lateGraceMinutes);
  setVal("rule-absent", rule.absentThresholdMinutes);
  setVal("rule-overtime", rule.overtimeThresholdMinutes);
}
