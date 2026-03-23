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
  department TEXT NOT NULL,
  title TEXT NOT NULL,
  phone TEXT,
  email TEXT,
  hire_date TEXT NOT NULL,
  status TEXT NOT NULL,
  department_id INTEGER,
  position_id INTEGER,
  grade_id INTEGER,
  manager_id INTEGER,
  FOREIGN KEY (department_id) REFERENCES departments(id),
  FOREIGN KEY (position_id) REFERENCES positions(id),
  FOREIGN KEY (grade_id) REFERENCES grades(id),
  FOREIGN KEY (manager_id) REFERENCES employees(id)
);

-- Attendance records per employee and date.
CREATE TABLE IF NOT EXISTS attendance (
  id INTEGER PRIMARY KEY AUTOINCREMENT,
  employee_id INTEGER NOT NULL,
  work_date TEXT NOT NULL,
  check_in TEXT,
  check_out TEXT,
  status TEXT NOT NULL,
  note TEXT,
  late_minutes INTEGER,
  overtime_minutes INTEGER,
  UNIQUE(employee_id, work_date),
  FOREIGN KEY (employee_id) REFERENCES employees(id)
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
  UNIQUE(employee_id, salary_month),
  FOREIGN KEY (employee_id) REFERENCES employees(id)
);

-- Shift schedule per employee/day
CREATE TABLE IF NOT EXISTS shifts (
  id INTEGER PRIMARY KEY AUTOINCREMENT,
  employee_id INTEGER NOT NULL,
  work_date TEXT NOT NULL,
  start_time TEXT NOT NULL,
  end_time TEXT NOT NULL,
  note TEXT,
  UNIQUE(employee_id, work_date),
  FOREIGN KEY (employee_id) REFERENCES employees(id)
);

-- Leave requests (approval workflow)
CREATE TABLE IF NOT EXISTS leave_requests (
  id INTEGER PRIMARY KEY AUTOINCREMENT,
  employee_id INTEGER NOT NULL,
  start_date TEXT NOT NULL,
  end_date TEXT NOT NULL,
  type TEXT NOT NULL,
  status TEXT NOT NULL,
  note TEXT,
  FOREIGN KEY (employee_id) REFERENCES employees(id)
);

-- Overtime requests (approval workflow)
CREATE TABLE IF NOT EXISTS overtime_requests (
  id INTEGER PRIMARY KEY AUTOINCREMENT,
  employee_id INTEGER NOT NULL,
  work_date TEXT NOT NULL,
  minutes INTEGER NOT NULL,
  status TEXT NOT NULL,
  note TEXT,
  FOREIGN KEY (employee_id) REFERENCES employees(id)
);

-- Attendance rule config (single row)
CREATE TABLE IF NOT EXISTS attendance_rules (
  id INTEGER PRIMARY KEY AUTOINCREMENT,
  late_grace_minutes INTEGER NOT NULL,
  overtime_threshold_minutes INTEGER NOT NULL
);

-- Department hierarchy (tree)
CREATE TABLE IF NOT EXISTS departments (
  id INTEGER PRIMARY KEY AUTOINCREMENT,
  name TEXT NOT NULL,
  parent_id INTEGER,
  UNIQUE(name, parent_id),
  FOREIGN KEY (parent_id) REFERENCES departments(id)
);

-- Position catalog
CREATE TABLE IF NOT EXISTS positions (
  id INTEGER PRIMARY KEY AUTOINCREMENT,
  name TEXT NOT NULL UNIQUE
);

-- Grade / level catalog
CREATE TABLE IF NOT EXISTS grades (
  id INTEGER PRIMARY KEY AUTOINCREMENT,
  name TEXT NOT NULL UNIQUE,
  level INTEGER NOT NULL
);

-- Role catalog
CREATE TABLE IF NOT EXISTS roles (
  id INTEGER PRIMARY KEY AUTOINCREMENT,
  name TEXT NOT NULL UNIQUE
);

-- Permission rules: which role can access which endpoint.
CREATE TABLE IF NOT EXISTS permissions (
  id INTEGER PRIMARY KEY AUTOINCREMENT,
  role TEXT NOT NULL,
  method TEXT NOT NULL,
  path_prefix TEXT NOT NULL,
  UNIQUE(role, method, path_prefix)
);
