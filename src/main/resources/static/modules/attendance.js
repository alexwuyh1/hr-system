async function loadAttendance() {
  const list = await apiRequest(API.attendance.list);
  const body = $("attendance-table").querySelector("tbody");
  body.innerHTML = "";
  list.forEach((a) => {
    const row = buildRow(
      [
        a.employee?.name || a.employee?.employeeNo || a.employee?.id || "-",
        a.workDate,
        a.checkIn || "-",
        a.checkOut || "-",
        a.status,
      ],
      async () => {
        await apiRequest(API.attendance.delete(a.id), { method: "DELETE" });
        await safeLoad("attendance", loadAttendance);
      }
    );
    body.appendChild(row);
  });
}

function initAttendanceForm() {
  $("attendance-form").addEventListener("submit", async (e) => {
    e.preventDefault();
    const data = Object.fromEntries(new FormData(e.target));
    data.employeeId = Number(data.employeeId);
    data.checkIn = normalizeEmptyToNull(data.checkIn);
    data.checkOut = normalizeEmptyToNull(data.checkOut);
    data.note = normalizeEmptyToNull(data.note);
    const error = validateAttendancePayload(data);
    if (error) {
      alert(error);
      return;
    }
    try {
      await apiRequest(API.attendance.create, {
        method: "POST",
        body: JSON.stringify(data),
      });
      e.target.reset();
      await loadAttendance();
    } catch (err) {
      alert(`保存失败: ${err.message}`);
    }
  });
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
