-- 修复 pm_dept 缺少 company_id 列（点击组织管理刷新报错时执行）
-- 执行前：USE pms;

SET NAMES utf8mb4;

-- 为 pm_dept 添加 company_id 列（若报 Duplicate column name 说明已有该列，可忽略）
ALTER TABLE pm_dept ADD COLUMN company_id BIGINT NOT NULL DEFAULT 1 COMMENT '所属公司ID' AFTER id;
ALTER TABLE pm_dept ADD KEY idx_company_id (company_id);
