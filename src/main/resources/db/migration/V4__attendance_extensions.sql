-- V4__attendance_extensions.sql
-- 扩展考勤规则和考勤记录表，支持更精细的考勤状态计算

ALTER TABLE attendance_rules ADD COLUMN early_leave_grace_minutes INTEGER DEFAULT 10;

ALTER TABLE attendance ADD COLUMN absent_minutes INTEGER;
ALTER TABLE attendance ADD COLUMN early_leave_minutes INTEGER;