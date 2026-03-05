# 项目管理系统 - 数据库设计文档

**版本**: v1.0  
**日期**: 2025-03-03

---

## 一、ER 概览

- **权限域**: sys_user, sys_role, sys_user_role, sys_menu, sys_role_menu, sys_dept, sys_role_data_scope
- **业务域**: pm_project, pm_task, pm_issue, pm_risk, pm_todo
- **审批域**: approval_definition, approval_instance, approval_node_instance, approval_opinion
- **配置域**: config_form_field, config_list_field, config_tab, sys_dict_type, sys_dict_item

---

## 二、表清单

| 表名 | 说明 |
|------|------|
| sys_user | 用户 |
| sys_role | 角色 |
| sys_user_role | 用户-角色关联 |
| sys_dept | 部门（用于数据权限） |
| sys_menu | 菜单/权限 |
| sys_role_menu | 角色-菜单 |
| sys_role_data_scope | 角色数据权限范围 |
| pm_project | 项目 |
| pm_task | 任务 |
| pm_issue | 问题 |
| pm_risk | 风险 |
| pm_todo | 个人待办 |
| approval_definition | 审批流程定义 |
| approval_instance | 审批实例 |
| approval_node_instance | 审批节点实例 |
| approval_opinion | 审批意见 |
| config_form_field | 表单字段配置 |
| config_list_field | 列表字段配置 |
| config_tab | 页签配置 |
| sys_dict_type | 数据字典类型 |
| sys_dict_item | 数据字典项 |

---

## 三、表结构说明

### 3.1 权限域

**sys_user**  
用户表：登录名、姓名、部门、状态、密码等。

**sys_role**  
角色表：角色编码、名称、数据范围类型等。

**sys_dept**  
部门表：树形结构，用于数据权限“本部门/本部门及下级”。

**sys_menu**  
菜单表：树形，名称、路由、图标、权限标识、排序。

**sys_role_menu**  
角色-菜单多对多。

**sys_role_data_scope**  
角色数据权限：范围类型（全部/本部门/本部门及下级/仅本人）、可选的部门 ID 等。

### 3.2 业务域

**pm_project**  
项目：名称、编码、负责人、开始/结束时间、状态等。

**pm_task**  
任务：所属项目、父任务、名称、负责人、开始/结束、进度、状态等，支持树形。

**pm_issue**  
问题：关联项目/任务、标题、严重程度、负责人、状态等。

**pm_risk**  
风险：关联项目、描述、等级、概率、影响、应对措施、负责人、状态等。

**pm_todo**  
待办：用户、类型（任务/问题/审批）、业务ID、标题、链接、状态、截止时间等。

### 3.3 审批域

**approval_definition**  
审批流程定义：类型编码、名称、业务类型、节点 JSON 或关联节点表。

**approval_instance**  
审批实例：关联定义、业务单号、发起人、当前节点、状态。

**approval_node_instance**  
节点实例：流程实例、节点序号、审批人、状态、处理时间。

**approval_opinion**  
审批意见：节点实例、审批人、通过/驳回、意见、时间。

### 3.4 配置域

**config_form_field**  
表单字段：业务类型、字段编码、名称、类型、必填、顺序、字典类型等。

**config_list_field**  
列表字段：业务类型、字段、列名、宽度、排序等。

**config_tab**  
页签：业务类型、页签编码、名称、关联组件/列表、顺序。

**sys_dict_type**  
字典类型：类型编码、名称。

**sys_dict_item**  
字典项：类型、项编码、名称、排序、状态。

---

## 四、索引与约束

- 各表主键均为 `id`（BIGINT 自增）。
- 外键字段建立普通索引便于查询（如 project_id, user_id, role_id）。
- 唯一约束：用户登录名、角色编码、项目编码、字典类型+项编码等见 DDL。
- 软删除：部分表设 `deleted` 或 `status`，按需在 DDL 中体现。

详细建表语句见 `../sql/schema.sql`。
