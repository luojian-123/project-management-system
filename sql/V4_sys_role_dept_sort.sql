-- 为 sys_role 表增加 dept_id、sort_order（角色归属部门与排序）
-- 执行前：USE pms;

SET NAMES utf8mb4;

-- 若列已存在会报 Duplicate column，可忽略
ALTER TABLE sys_role ADD COLUMN dept_id BIGINT DEFAULT NULL COMMENT '所属部门ID' AFTER status;
ALTER TABLE sys_role ADD COLUMN sort_order INT DEFAULT 0 AFTER dept_id;
ALTER TABLE sys_role ADD KEY idx_dept_id (dept_id);
