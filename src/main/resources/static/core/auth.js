let authToken = localStorage.getItem("hr_token");

function persistToken(token) {
  authToken = token;
  if (token) {
    localStorage.setItem("hr_token", token);
  } else {
    localStorage.removeItem("hr_token");
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
    const username = prompt("输入新用户名");
    const password = prompt("输入新密码");
    if (!username || !password) {
      return;
    }
    try {
      await apiRequest(API.auth.register, {
        method: "POST",
        body: JSON.stringify({ username, password }),
      });
      alert("注册成功，请登录");
    } catch (err) {
      alert(`注册失败: ${err.message}`);
    }
  });

  const logoutBtn = $("logout");
  if (logoutBtn) {
    logoutBtn.addEventListener("click", () => {
      persistToken(null);
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
          $("auth-panel").classList.remove("hidden");
          $("workspace").classList.add("hidden");
          return;
        }
        
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
