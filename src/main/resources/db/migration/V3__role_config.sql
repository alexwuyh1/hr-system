-- V3__role_config.sql
-- 新建 role_config 表，将角色的名单模式从 permissions 表中分离

CREATE TABLE IF NOT EXISTS role_config (
  id INTEGER PRIMARY KEY AUTOINCREMENT,
  role TEXT NOT NULL UNIQUE,
  role_mode TEXT NOT NULL CHECK(role_mode IN ('whitelist', 'blacklist'))
);

-- 迁移现有数据：从 permissions 表中提取角色的 role_mode
INSERT OR IGNORE INTO role_config (role, role_mode)
SELECT DISTINCT role, COALESCE(role_mode, 'whitelist')
FROM permissions;

-- 清理 permissions 表中的冗余字段
CREATE TABLE IF NOT EXISTS permissions_new (
  id INTEGER PRIMARY KEY AUTOINCREMENT,
  role TEXT NOT NULL,
  method TEXT NOT NULL,
  path_prefix TEXT NOT NULL,
  UNIQUE(role, method, path_prefix)
);

INSERT INTO permissions_new (id, role, method, path_prefix)
SELECT id, role, method, path_prefix FROM permissions;

DROP TABLE IF EXISTS permissions;
ALTER TABLE permissions_new RENAME TO permissions;
