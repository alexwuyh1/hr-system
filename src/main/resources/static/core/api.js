const API_BASE = "/api";

const API = {
  auth: { login: "/auth/login", register: "/auth/register" },
  dashboard: { summary: "/dashboard/summary" },
  employees: {
    list: "/employees", create: "/employees", resign: "/employees/resign", rehire: "/employees/rehire",
    update: (id) => `/employees/${id}`, delete: (id) => `/employees/${id}`, avatar: (id) => `/employees/${id}/avatar`,
  },
  attendance: {
    list: "/attendance", create: "/attendance",
    update: (id) => `/attendance/${id}`, delete: (id) => `/attendance/${id}`,
  },
  attendanceRules: {
    get: "/attendance-rules", update: "/attendance-rules",
    calculate: (date) => `/attendance-rules/calculate?date=${date}`,
    calculateRange: (start, end) => `/attendance-rules/calculate-range?start=${start}&end=${end}`,
  },
  salaries: {
    list: "/salaries", create: "/salaries",
    update: (id) => `/salaries/${id}`, delete: (id) => `/salaries/${id}`,
  },
  organizations: {
    list: "/organizations", tree: "/organizations/tree", positionTree: "/organizations/position-tree", create: "/organizations",
    update: (id) => `/organizations/${id}`, delete: (id) => `/organizations/${id}`,
  },
  permissions: { list: "/permissions", roles: "/permissions/roles", create: "/permissions", update: (id) => `/permissions/${id}`, delete: (id) => `/permissions/${id}` },
  users: { list: "/users", me: "/users/me" },
  data: { export: (type, format) => `/data/export/${type}?format=${format}`, import: (type) => `/data/import/${type}` },
  init: "/init",
};
