-- Users table: store login credentials and role.
CREATE TABLE IF NOT EXISTS users (
  id INTEGER PRIMARY KEY AUTOINCREMENT,
  username TEXT NOT NULL UNIQUE,
  password_hash TEXT NOT NULL,
  role TEXT NOT NULL,
  created_at INTEGER NOT NULL
);

-- Employee master data.
CREATE TABLE IF NOT EXISTS employees (
  id INTEGER PRIMARY KEY AUTOINCREMENT,
  employee_no TEXT NOT NULL UNIQUE,
  name TEXT NOT NULL,
  phone TEXT,
  email TEXT,
  hire_date TEXT NOT NULL,
  status TEXT NOT NULL,
  org_id INTEGER,
  position_id INTEGER,
  manager_id INTEGER,
  avatar_path TEXT,
  face_hash TEXT,
  FOREIGN KEY (org_id) REFERENCES organizations(id) ON DELETE SET NULL,
  FOREIGN KEY (position_id) REFERENCES organizations(id) ON DELETE SET NULL,
  FOREIGN KEY (manager_id) REFERENCES employees(id) ON DELETE SET NULL
);

-- Attendance records per employee and date.
CREATE TABLE IF NOT EXISTS attendance (
  id INTEGER PRIMARY KEY AUTOINCREMENT,
  employee_id INTEGER NOT NULL,
  work_date TEXT NOT NULL,
  check_in TEXT,
  check_out TEXT,
  status TEXT,
  late_minutes INTEGER,
  note TEXT,
  FOREIGN KEY (employee_id) REFERENCES employees(id) ON DELETE CASCADE
);

-- Salary records per employee and month.
CREATE TABLE IF NOT EXISTS salaries (
  id INTEGER PRIMARY KEY AUTOINCREMENT,
  employee_id INTEGER NOT NULL,
  salary_month TEXT NOT NULL,
  base_salary REAL NOT NULL,
  bonus REAL NOT NULL,
  deduction REAL NOT NULL,
  note TEXT,
  FOREIGN KEY (employee_id) REFERENCES employees(id) ON DELETE CASCADE
);

-- Attendance rule config (single row)
CREATE TABLE IF NOT EXISTS attendance_rules (
  id INTEGER PRIMARY KEY AUTOINCREMENT,
  work_start_time TEXT,
  work_end_time TEXT,
  late_grace_minutes INTEGER NOT NULL,
  absent_threshold_minutes INTEGER
);

-- Organization table (merged department/position/grade)
CREATE TABLE IF NOT EXISTS organizations (
  id INTEGER PRIMARY KEY AUTOINCREMENT,
  name TEXT NOT NULL,
  type TEXT NOT NULL,
  parent_id INTEGER,
  level INTEGER,
  grade_id INTEGER,
  FOREIGN KEY (parent_id) REFERENCES organizations(id) ON DELETE SET NULL,
  FOREIGN KEY (grade_id) REFERENCES organizations(id) ON DELETE SET NULL
);

-- Permission rules: which role can access which endpoint.
CREATE TABLE IF NOT EXISTS permissions (
  id INTEGER PRIMARY KEY AUTOINCREMENT,
  role TEXT NOT NULL,
  method TEXT NOT NULL,
  path_prefix TEXT NOT NULL,
  mode TEXT DEFAULT 'allow',
  role_mode TEXT,
  UNIQUE(role, method, path_prefix, mode)
);

-- Indexes for query performance
CREATE INDEX IF NOT EXISTS idx_employees_status ON employees(status);
CREATE INDEX IF NOT EXISTS idx_employees_org ON employees(org_id);
CREATE INDEX IF NOT EXISTS idx_attendance_date ON attendance(work_date);
CREATE INDEX IF NOT EXISTS idx_attendance_status ON attendance(status);
CREATE INDEX IF NOT EXISTS idx_salaries_month ON salaries(salary_month);
CREATE INDEX IF NOT EXISTS idx_organizations_type ON organizations(type);
CREATE INDEX IF NOT EXISTS idx_organizations_parent ON organizations(parent_id);
CREATE INDEX IF NOT EXISTS idx_permissions_role ON permissions(role);
