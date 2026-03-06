-- 项目管理管理系统 - 数据库建表脚本 MySQL 8.x
-- 执行前请创建数据库: CREATE DATABASE pms DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

SET NAMES utf8mb4;
USE pms;

-- ----------------------------
-- 1. 系统权限
-- ----------------------------
CREATE TABLE sys_user (
  id          BIGINT PRIMARY KEY AUTO_INCREMENT,
  username    VARCHAR(64) NOT NULL COMMENT '登录名',
  password    VARCHAR(256) NOT NULL COMMENT '密码(加密)',
  real_name   VARCHAR(64) DEFAULT NULL COMMENT '真实姓名',
  email       VARCHAR(128) DEFAULT NULL,
  phone       VARCHAR(32) DEFAULT NULL,
  status      TINYINT DEFAULT 1 COMMENT '1启用 0禁用',
  created_at  DATETIME DEFAULT CURRENT_TIMESTAMP,
  updated_at  DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  UNIQUE KEY uk_username (username)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户';

CREATE TABLE sys_role (
  id          BIGINT PRIMARY KEY AUTO_INCREMENT,
  code        VARCHAR(64) NOT NULL COMMENT '角色编码',
  name        VARCHAR(64) NOT NULL COMMENT '角色名称',
  status      TINYINT DEFAULT 1,
  dept_id     BIGINT DEFAULT NULL COMMENT '所属部门ID',
  sort_order  INT DEFAULT 0,
  created_at  DATETIME DEFAULT CURRENT_TIMESTAMP,
  updated_at  DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  UNIQUE KEY uk_code (code),
  KEY idx_dept_id (dept_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色';

CREATE TABLE sys_menu (
  id          BIGINT PRIMARY KEY AUTO_INCREMENT,
  parent_id   BIGINT DEFAULT 0 COMMENT '父菜单ID',
  name        VARCHAR(64) NOT NULL COMMENT '菜单名称',
  path        VARCHAR(128) DEFAULT NULL COMMENT '路由路径',
  component   VARCHAR(128) DEFAULT NULL,
  permission  VARCHAR(128) DEFAULT NULL COMMENT '权限标识',
  type        TINYINT DEFAULT 1 COMMENT '1菜单 2按钮',
  sort_order  INT DEFAULT 0,
  icon        VARCHAR(64) DEFAULT NULL,
  status      TINYINT DEFAULT 1,
  created_at  DATETIME DEFAULT CURRENT_TIMESTAMP,
  updated_at  DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='菜单';

CREATE TABLE sys_user_role (
  id         BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id    BIGINT NOT NULL,
  role_id    BIGINT NOT NULL,
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  UNIQUE KEY uk_user_role (user_id, role_id),
  KEY idx_user_id (user_id),
  KEY idx_role_id (role_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户-角色';

CREATE TABLE sys_role_menu (
  id         BIGINT PRIMARY KEY AUTO_INCREMENT,
  role_id    BIGINT NOT NULL,
  menu_id    BIGINT NOT NULL,
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  UNIQUE KEY uk_role_menu (role_id, menu_id),
  KEY idx_role_id (role_id),
  KEY idx_menu_id (menu_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色-菜单';

-- ----------------------------
-- 2. 字典与系统配置
-- ----------------------------
CREATE TABLE sys_dict_type (
  id          BIGINT PRIMARY KEY AUTO_INCREMENT,
  code        VARCHAR(64) NOT NULL COMMENT '字典类型编码',
  name        VARCHAR(64) NOT NULL COMMENT '字典类型名称',
  status      TINYINT DEFAULT 1,
  created_at  DATETIME DEFAULT CURRENT_TIMESTAMP,
  updated_at  DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  UNIQUE KEY uk_code (code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='字典类型';

CREATE TABLE sys_dict_item (
  id          BIGINT PRIMARY KEY AUTO_INCREMENT,
  type_code   VARCHAR(64) NOT NULL COMMENT '字典类型编码',
  item_key    VARCHAR(64) NOT NULL COMMENT '键',
  item_value  VARCHAR(256) NOT NULL COMMENT '显示值',
  sort_order  INT DEFAULT 0,
  status      TINYINT DEFAULT 1,
  created_at  DATETIME DEFAULT CURRENT_TIMESTAMP,
  updated_at  DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  KEY idx_type_code (type_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='字典项';

CREATE TABLE sys_approval_flow (
  id          BIGINT PRIMARY KEY AUTO_INCREMENT,
  code        VARCHAR(64) NOT NULL COMMENT '流程编码',
  name        VARCHAR(128) NOT NULL COMMENT '流程名称',
  biz_type    VARCHAR(64) DEFAULT NULL COMMENT '业务类型',
  status      TINYINT DEFAULT 1,
  created_at  DATETIME DEFAULT CURRENT_TIMESTAMP,
  updated_at  DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  UNIQUE KEY uk_code (code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='审批流定义';

CREATE TABLE sys_approval_node (
  id           BIGINT PRIMARY KEY AUTO_INCREMENT,
  flow_id      BIGINT NOT NULL COMMENT '审批流ID',
  node_order   INT NOT NULL COMMENT '节点顺序',
  node_name    VARCHAR(64) NOT NULL COMMENT '节点名称',
  approver_type VARCHAR(32) DEFAULT 'ROLE' COMMENT 'ROLE/USER',
  approver_id  VARCHAR(128) DEFAULT NULL COMMENT '角色ID或用户ID，多个逗号分隔',
  created_at   DATETIME DEFAULT CURRENT_TIMESTAMP,
  updated_at   DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  KEY idx_flow_id (flow_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='审批节点';

CREATE TABLE sys_form_config (
  id          BIGINT PRIMARY KEY AUTO_INCREMENT,
  form_code   VARCHAR(64) NOT NULL COMMENT '表单编码',
  form_name   VARCHAR(128) NOT NULL COMMENT '表单名称',
  biz_type    VARCHAR(64) DEFAULT NULL COMMENT '业务类型',
  status      TINYINT DEFAULT 1,
  created_at  DATETIME DEFAULT CURRENT_TIMESTAMP,
  updated_at  DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  UNIQUE KEY uk_form_code (form_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='表单配置';

CREATE TABLE sys_form_field (
  id           BIGINT PRIMARY KEY AUTO_INCREMENT,
  form_id      BIGINT NOT NULL COMMENT '表单配置ID',
  field_name   VARCHAR(64) NOT NULL COMMENT '字段名',
  field_label  VARCHAR(64) NOT NULL COMMENT '显示标签',
  field_type   VARCHAR(32) DEFAULT 'input' COMMENT 'input/select/date/number等',
  required     TINYINT DEFAULT 0 COMMENT '是否必填',
  sort_order   INT DEFAULT 0,
  extra_json   JSON DEFAULT NULL COMMENT '校验、选项等扩展',
  created_at   DATETIME DEFAULT CURRENT_TIMESTAMP,
  updated_at   DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  KEY idx_form_id (form_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='表单字段';

CREATE TABLE sys_tab_config (
  id          BIGINT PRIMARY KEY AUTO_INCREMENT,
  biz_type    VARCHAR(64) NOT NULL COMMENT '业务类型 如 project/task',
  tab_code    VARCHAR(64) NOT NULL COMMENT '页签编码',
  tab_name    VARCHAR(64) NOT NULL COMMENT '页签名称',
  tab_path    VARCHAR(256) DEFAULT NULL COMMENT '前端路由或组件路径',
  sort_order  INT DEFAULT 0,
  status      TINYINT DEFAULT 1,
  created_at  DATETIME DEFAULT CURRENT_TIMESTAMP,
  updated_at  DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  KEY idx_biz_type (biz_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='关联页签配置';

-- ----------------------------
-- 3. 项目与任务
-- ----------------------------
CREATE TABLE pm_project (
  id           BIGINT PRIMARY KEY AUTO_INCREMENT,
  project_code VARCHAR(64) NOT NULL COMMENT '项目编码',
  project_name VARCHAR(128) NOT NULL COMMENT '项目名称',
  description  TEXT DEFAULT NULL,
  owner_id     BIGINT DEFAULT NULL COMMENT '负责人用户ID',
  plan_start   DATE DEFAULT NULL COMMENT '计划开始',
  plan_end     DATE DEFAULT NULL COMMENT '计划结束',
  status       VARCHAR(32) DEFAULT 'PLANNING' COMMENT 'PLANNING/IN_PROGRESS/PAUSED/CLOSED',
  created_by   BIGINT DEFAULT NULL,
  created_at   DATETIME DEFAULT CURRENT_TIMESTAMP,
  updated_at   DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  UNIQUE KEY uk_project_code (project_code),
  KEY idx_owner (owner_id),
  KEY idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='项目';

CREATE TABLE pm_task (
  id            BIGINT PRIMARY KEY AUTO_INCREMENT,
  project_id    BIGINT NOT NULL COMMENT '项目ID',
  parent_id     BIGINT DEFAULT 0 COMMENT '父任务ID',
  task_code     VARCHAR(64) NOT NULL COMMENT '任务编码(项目内唯一)',
  task_name     VARCHAR(256) NOT NULL COMMENT '任务名称',
  assignee_id   BIGINT DEFAULT NULL COMMENT '负责人',
  plan_start    DATE DEFAULT NULL,
  plan_end      DATE DEFAULT NULL,
  actual_start  DATE DEFAULT NULL,
  actual_end    DATE DEFAULT NULL,
  progress      INT DEFAULT 0 COMMENT '进度0-100',
  status        VARCHAR(32) DEFAULT 'TODO' COMMENT 'TODO/IN_PROGRESS/DONE/CANCELLED',
  sort_order    INT DEFAULT 0,
  created_by    BIGINT DEFAULT NULL,
  created_at    DATETIME DEFAULT CURRENT_TIMESTAMP,
  updated_at    DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  KEY idx_project (project_id),
  KEY idx_parent (parent_id),
  KEY idx_assignee (assignee_id),
  KEY idx_status (status),
  UNIQUE KEY uk_project_task_code (project_id, task_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='任务';

CREATE TABLE pm_task_dependency (
  id             BIGINT PRIMARY KEY AUTO_INCREMENT,
  task_id        BIGINT NOT NULL COMMENT '当前任务',
  depend_task_id BIGINT NOT NULL COMMENT '依赖的任务(前置)',
  dep_type       VARCHAR(16) DEFAULT 'FS' COMMENT 'FS/SS等',
  created_at     DATETIME DEFAULT CURRENT_TIMESTAMP,
  UNIQUE KEY uk_task_depend (task_id, depend_task_id),
  KEY idx_task_id (task_id),
  KEY idx_depend_task_id (depend_task_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='任务依赖';

-- ----------------------------
-- 4. 个人待办
-- ----------------------------
CREATE TABLE pm_todo (
  id           BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id      BIGINT NOT NULL COMMENT '所属用户',
  title        VARCHAR(256) NOT NULL COMMENT '标题',
  biz_type     VARCHAR(32) DEFAULT NULL COMMENT 'task/issue/change等',
  biz_id       BIGINT DEFAULT NULL COMMENT '关联业务ID',
  priority     VARCHAR(16) DEFAULT 'NORMAL' COMMENT 'HIGH/NORMAL/LOW',
  due_date     DATE DEFAULT NULL COMMENT '截止日期',
  status       VARCHAR(32) DEFAULT 'PENDING' COMMENT 'PENDING/IN_PROGRESS/DONE',
  created_at   DATETIME DEFAULT CURRENT_TIMESTAMP,
  updated_at   DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  KEY idx_user (user_id),
  KEY idx_status (status),
  KEY idx_due_date (due_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='个人待办';

-- ----------------------------
-- 5. 成本 / 问题 / 变更 / 风险
-- ----------------------------
CREATE TABLE pm_cost (
  id          BIGINT PRIMARY KEY AUTO_INCREMENT,
  project_id  BIGINT NOT NULL,
  task_id     BIGINT DEFAULT NULL COMMENT '可选，关联任务',
  cost_type   VARCHAR(32) NOT NULL COMMENT 'MANPOWER/MATERIAL/OTHER',
  budget_amount DECIMAL(18,2) DEFAULT 0.00 COMMENT '预算金额',
  actual_amount DECIMAL(18,2) DEFAULT 0.00 COMMENT '实际金额',
  occur_date  DATE DEFAULT NULL COMMENT '发生日期',
  remark      VARCHAR(512) DEFAULT NULL,
  created_by  BIGINT DEFAULT NULL,
  created_at  DATETIME DEFAULT CURRENT_TIMESTAMP,
  updated_at  DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  KEY idx_project (project_id),
  KEY idx_task (task_id),
  KEY idx_occur_date (occur_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='成本记录';

CREATE TABLE pm_issue (
  id           BIGINT PRIMARY KEY AUTO_INCREMENT,
  project_id   BIGINT NOT NULL,
  task_id      BIGINT DEFAULT NULL COMMENT '关联任务(可选)',
  issue_code   VARCHAR(64) NOT NULL COMMENT '问题编码',
  title        VARCHAR(256) NOT NULL,
  description  TEXT DEFAULT NULL,
  severity     VARCHAR(32) DEFAULT 'MEDIUM' COMMENT 'LOW/MEDIUM/HIGH/CRITICAL',
  status       VARCHAR(32) DEFAULT 'OPEN' COMMENT 'OPEN/IN_PROGRESS/RESOLVED/CLOSED',
  assignee_id  BIGINT DEFAULT NULL COMMENT '责任人',
  found_date   DATE DEFAULT NULL,
  resolved_date DATE DEFAULT NULL,
  created_by   BIGINT DEFAULT NULL,
  created_at   DATETIME DEFAULT CURRENT_TIMESTAMP,
  updated_at   DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  UNIQUE KEY uk_issue_code (issue_code),
  KEY idx_project (project_id),
  KEY idx_task (task_id),
  KEY idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='问题';

CREATE TABLE pm_change (
  id            BIGINT PRIMARY KEY AUTO_INCREMENT,
  project_id    BIGINT NOT NULL,
  change_no     VARCHAR(64) NOT NULL COMMENT '变更单号',
  title         VARCHAR(256) NOT NULL,
  description   TEXT DEFAULT NULL,
  change_type   VARCHAR(32) DEFAULT NULL COMMENT '字典或枚举',
  impact_scope  VARCHAR(512) DEFAULT NULL COMMENT '影响范围',
  status        VARCHAR(32) DEFAULT 'DRAFT' COMMENT 'DRAFT/PENDING/APPROVED/REJECTED',
  applicant_id  BIGINT DEFAULT NULL,
  apply_time    DATETIME DEFAULT NULL,
  flow_id       BIGINT DEFAULT NULL COMMENT '关联审批流',
  created_at    DATETIME DEFAULT CURRENT_TIMESTAMP,
  updated_at    DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  UNIQUE KEY uk_change_no (change_no),
  KEY idx_project (project_id),
  KEY idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='变更';

CREATE TABLE pm_risk (
  id           BIGINT PRIMARY KEY AUTO_INCREMENT,
  project_id   BIGINT NOT NULL,
  risk_code    VARCHAR(64) NOT NULL COMMENT '风险编码',
  title        VARCHAR(256) NOT NULL,
  description  TEXT DEFAULT NULL,
  probability  VARCHAR(16) DEFAULT NULL COMMENT 'LOW/MEDIUM/HIGH',
  impact       VARCHAR(16) DEFAULT NULL COMMENT 'LOW/MEDIUM/HIGH',
  risk_level   VARCHAR(16) DEFAULT NULL COMMENT '计算或手工填',
  response     TEXT DEFAULT NULL COMMENT '应对措施',
  owner_id     BIGINT DEFAULT NULL COMMENT '责任人',
  status       VARCHAR(32) DEFAULT 'IDENTIFIED' COMMENT 'IDENTIFIED/MITIGATING/CLOSED',
  created_by   BIGINT DEFAULT NULL,
  created_at   DATETIME DEFAULT CURRENT_TIMESTAMP,
  updated_at   DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  UNIQUE KEY uk_risk_code (risk_code),
  KEY idx_project (project_id),
  KEY idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='风险';

-- ----------------------------
-- 6. 组织（公司、部门）
-- ----------------------------
CREATE TABLE pm_company (
  id           BIGINT PRIMARY KEY AUTO_INCREMENT,
  company_code VARCHAR(64) NOT NULL COMMENT '公司编码',
  company_name VARCHAR(128) NOT NULL COMMENT '公司名称',
  sort_order   INT DEFAULT 0,
  created_at   DATETIME DEFAULT CURRENT_TIMESTAMP,
  updated_at   DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  UNIQUE KEY uk_company_code (company_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='公司';

CREATE TABLE pm_dept (
  id           BIGINT PRIMARY KEY AUTO_INCREMENT,
  company_id   BIGINT NOT NULL COMMENT '所属公司ID',
  dept_code    VARCHAR(64) NOT NULL COMMENT '部门编码',
  dept_name    VARCHAR(128) NOT NULL COMMENT '部门名称',
  sort_order   INT DEFAULT 0,
  created_at   DATETIME DEFAULT CURRENT_TIMESTAMP,
  updated_at   DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  KEY idx_company_id (company_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='部门';

-- 初始化角色与菜单（可选）；管理员用户由应用启动时自动创建（用户名 admin 密码 123456）
INSERT INTO sys_role (code, name) VALUES ('ADMIN', '管理员'), ('PM', '项目经理'), ('MEMBER', '成员');
INSERT INTO sys_menu (parent_id, name, path, component, type, sort_order, icon) VALUES
(0, '个人待办', '/todo', 'Todo', 1, 1, 'List'),
(0, '项目管理', '/project', 'Project', 1, 2, 'Folder'),
(0, '成本管理', '/cost', 'Cost', 1, 3, 'Money'),
(0, '问题管理', '/issue', 'Issue', 1, 4, 'Warning'),
(0, '变更管理', '/change', 'Change', 1, 5, 'Edit'),
(0, '风险管理', '/risk', 'Risk', 1, 6, 'Flag'),
(0, '系统配置', '/sys', 'Sys', 1, 7, 'Setting');
