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
  status TEXT NOT NULL
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
