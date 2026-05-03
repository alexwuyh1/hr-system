let authToken = localStorage.getItem("hr_token");
let currentRole = localStorage.getItem("hr_role");

function persistToken(token) {
  authToken = token;
  if (token) {
    localStorage.setItem("hr_token", token);
  } else {
    localStorage.removeItem("hr_token");
  }
}

function setRole(role) {
  currentRole = role;
  if (role) {
    localStorage.setItem("hr_role", role);
  } else {
    localStorage.removeItem("hr_role");
  }
}

function updateRoleBadge() {
  const badge = $("role-badge");
  if (badge && currentRole) {
    badge.textContent = currentRole;
  }
}

async function apiRequest(path, options = {}) {
  const headers = options.headers || {};
  if (authToken) {
    headers.Authorization = `Bearer ${authToken}`;
  }
  const response = await fetch(`${API_BASE}${path}`, {
    ...options,
    headers: {
      "Content-Type": "application/json",
      ...headers,
    },
  });
  const contentType = response.headers.get("content-type") || "";
  if (!response.ok) {
    const error = await response.text();
    throw new Error(error || "Request failed");
  }
  if (response.status === 204) {
    return null;
  }
  const text = await response.text();
  if (!text) {
    return null;
  }
  if (contentType.includes("application/json")) {
    return JSON.parse(text);
  }
  return text;
}

function initAuth() {
  $("login-form").addEventListener("submit", async (e) => {
    e.preventDefault();
    const data = Object.fromEntries(new FormData(e.target));
    try {
      const result = await apiRequest(API.auth.login, {
        method: "POST",
        body: JSON.stringify(data),
      });
      persistToken(result.token);
      setRole(result.role);
      updateRoleBadge();
      $("auth-panel").classList.add("hidden");
      $("workspace").classList.remove("hidden");
      switchTab("dashboard");
      await safeLoad("init", loadInit);
      await ensureTabData("dashboard");
    } catch (err) {
      alert(`登录失败: ${err.message}`);
    }
  });

  $("register-btn").addEventListener("click", async () => {
    openModal(
      '注册新用户',
      `
        <form id="modal-register-form">
          <label>用户名 <input name="username" required placeholder="输入用户名"></label>
          <label>密码 <input name="password" type="password" required placeholder="输入密码"></label>
          <label>角色 <select name="role" required>
            <option value="">请选择角色</option>
            <option value="管理员">管理员</option>
            <option value="人事">人事</option>
            <option value="员工">员工</option>
          </select></label>
        </form>
      `,
      async (formData) => {
        await apiRequest(API.auth.register, {
          method: "POST",
          body: JSON.stringify(formData),
        });
        alert("注册成功，请登录");
      },
      { submitText: '注册' }
    );
  });

  const logoutBtn = $("logout");
  if (logoutBtn) {
    logoutBtn.addEventListener("click", () => {
      persistToken(null);
      setRole(null);
      $("workspace").classList.add("hidden");
      $("auth-panel").classList.remove("hidden");
    });
  }

  if (authToken) {
    (async () => {
      try {
        const response = await fetch(`${API_BASE}${API.dashboard.summary}`, {
          headers: {
            Authorization: `Bearer ${authToken}`
          }
        });
        
        if (!response.ok) {
          persistToken(null);
          setRole(null);
          $("auth-panel").classList.remove("hidden");
          $("workspace").classList.add("hidden");
          return;
        }

        if (!currentRole) {
          const users = await apiRequest(API.users.me);
          if (users && users.role) {
            setRole(users.role);
          }
        }
        
        updateRoleBadge();
        $("auth-panel").classList.add("hidden");
        $("workspace").classList.remove("hidden");
        switchTab("dashboard");
        await safeLoad("init", loadInit);
        await ensureTabData("dashboard");
      } catch (err) {
        console.error("自动登录失败:", err);
        persistToken(null);
        $("auth-panel").classList.remove("hidden");
        $("workspace").classList.add("hidden");
      }
    })();
  }
}
