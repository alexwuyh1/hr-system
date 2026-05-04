-- V2__add_indexes.sql
-- 为高频查询字段添加索引

CREATE INDEX IF NOT EXISTS idx_employees_employee_no ON employees(employee_no);
CREATE INDEX IF NOT EXISTS idx_employees_status ON employees(status);
CREATE INDEX IF NOT EXISTS idx_employees_org_id ON employees(org_id);

CREATE INDEX IF NOT EXISTS idx_attendance_employee_id ON attendance(employee_id);
CREATE INDEX IF NOT EXISTS idx_attendance_work_date ON attendance(work_date);
CREATE INDEX IF NOT EXISTS idx_attendance_employee_date ON attendance(employee_id, work_date);

CREATE INDEX IF NOT EXISTS idx_salaries_employee_id ON salaries(employee_id);
CREATE INDEX IF NOT EXISTS idx_salaries_month ON salaries(salary_month);
CREATE INDEX IF NOT EXISTS idx_salaries_employee_month ON salaries(employee_id, salary_month);

CREATE INDEX IF NOT EXISTS idx_organizations_type ON organizations(type);
CREATE INDEX IF NOT EXISTS idx_organizations_parent_id ON organizations(parent_id);

CREATE INDEX IF NOT EXISTS idx_permissions_role ON permissions(role);
CREATE INDEX IF NOT EXISTS idx_permissions_role_method_mode ON permissions(role, method, mode);
