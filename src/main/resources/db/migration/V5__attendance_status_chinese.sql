-- V5__attendance_status_chinese.sql
-- 统一考勤状态为中文

UPDATE attendance SET status = '正常' WHERE status = 'Normal';
UPDATE attendance SET status = '迟到' WHERE status = 'Late';
UPDATE attendance SET status = '正常' WHERE status = '正常' AND late_minutes IS NULL AND absent_minutes IS NULL AND early_leave_minutes IS NULL;
