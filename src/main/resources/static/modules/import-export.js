function initImportExportTab() {
  const importBtn = $("import-btn");
  if (importBtn) {
    importBtn.onclick = () => openImportModal();
  }

  const exportBtn = $("export-btn");
  if (exportBtn) {
    exportBtn.onclick = () => openExportModal();
  }
}

function openImportModal() {
  openModal(
    '导入数据',
    `
      <form id="modal-import-form">
        <label>导入类型 <select id="modal-import-type" required>
          <option value="employees">员工</option>
          <option value="attendance">考勤</option>
          <option value="salary">薪资</option>
        </select></label>
        <label>选择文件 <input id="modal-import-file" type="file" accept=".csv,.xlsx,.xls" required></label>
      </form>
    `,
    async () => {
      const type = $("modal-import-type").value;
      const file = $("modal-import-file").files[0];
      if (!file) {
        alert("请选择文件");
        return;
      }
      const form = new FormData();
      form.append("file", file);
      const response = await fetch(`${API_BASE}${API.data.import(type)}`, {
        method: "POST",
        headers: authToken ? { Authorization: `Bearer ${authToken}` } : {},
        body: form,
      });
      if (!response.ok) {
        const error = await response.text();
        throw new Error(error || "导入失败");
      }
      const result = await response.json();
      $("import-result").textContent = `导入成功：${result.imported} 条`;
    },
    { submitText: '导入' }
  );
}

function openExportModal() {
  openModal(
    '导出数据',
    `
      <form id="modal-export-form">
        <label>导出类型 <select id="modal-export-type" required>
          <option value="employees">员工</option>
          <option value="attendance">考勤</option>
          <option value="salary">薪资</option>
        </select></label>
        <label>导出格式 <select id="modal-export-format" required>
          <option value="csv">CSV</option>
          <option value="xlsx">Excel</option>
        </select></label>
      </form>
    `,
    async () => {
      const type = $("modal-export-type").value;
      const format = $("modal-export-format").value;
      const url = API.data.export(type, format);

      if (!authToken) {
        alert("请先登录");
        $("auth-panel").classList.remove("hidden");
        $("workspace").classList.add("hidden");
        return;
      }

      try {
        const headers = {};
        if (authToken) {
          headers.Authorization = `Bearer ${authToken}`;
        }

        const response = await fetch(`${API_BASE}${url}`, { headers });

        if (!response.ok) {
          if (response.status === 403) {
            alert("认证已过期，请重新登录");
            persistToken(null);
            $("auth-panel").classList.remove("hidden");
            $("workspace").classList.add("hidden");
            return;
          }
          const error = await response.text();
          throw new Error(error || "导出失败");
        }

        const blob = await response.blob();
        const filename = `${type}.${format}`;

        const downloadUrl = window.URL.createObjectURL(blob);
        const a = document.createElement("a");
        a.href = downloadUrl;
        a.download = filename;
        document.body.appendChild(a);
        a.click();
        document.body.removeChild(a);
        window.URL.revokeObjectURL(downloadUrl);
      } catch (err) {
        console.error("导出失败:", err);
        alert(`导出失败：${err.message}`);
      }
    },
    { submitText: '导出' }
  );
}
