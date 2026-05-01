function initImportExport() {
  const dataImportBtn = $("data-import");
  if (dataImportBtn) {
    dataImportBtn.addEventListener("click", async () => {
      const type = $("data-type-select").value;
      const format = $("data-format-select").value;
      const fileInput = $("data-file-input");
      const file = fileInput.files[0];
      if (!file) {
        alert("请选择文件");
        return;
      }
      const form = new FormData();
      form.append("file", file);
      try {
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
        $("data-result").textContent = `导入成功：${result.imported} 条`;
      } catch (err) {
        $("data-result").textContent = `导入失败：${err.message}`;
      }
    });
  }

  const dataExportBtn = $("data-export");
  if (dataExportBtn) {
    dataExportBtn.addEventListener("click", async () => {
      const type = $("data-type-select").value;
      const format = $("data-format-select").value;
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
    });
  }
}
