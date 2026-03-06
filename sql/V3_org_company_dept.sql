-- 组织管理：公司、部门表（若 schema 中已包含可跳过）
-- 执行前请确认数据库：USE pms;

SET NAMES utf8mb4;

-- 公司表（不存在则创建）
CREATE TABLE IF NOT EXISTS pm_company (
  id           BIGINT PRIMARY KEY AUTO_INCREMENT,
  company_code VARCHAR(64) NOT NULL COMMENT '公司编码',
  company_name VARCHAR(128) NOT NULL COMMENT '公司名称',
  sort_order   INT DEFAULT 0,
  created_at   DATETIME DEFAULT CURRENT_TIMESTAMP,
  updated_at   DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  UNIQUE KEY uk_company_code (company_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='公司';

-- 部门表（不存在则创建）
CREATE TABLE IF NOT EXISTS pm_dept (
  id           BIGINT PRIMARY KEY AUTO_INCREMENT,
  company_id   BIGINT NOT NULL COMMENT '所属公司ID',
  dept_code    VARCHAR(64) NOT NULL COMMENT '部门编码',
  dept_name    VARCHAR(128) NOT NULL COMMENT '部门名称',
  sort_order   INT DEFAULT 0,
  created_at   DATETIME DEFAULT CURRENT_TIMESTAMP,
  updated_at   DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  KEY idx_company_id (company_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='部门';
