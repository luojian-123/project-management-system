-- 表单/菜单/字段配置驱动建模：扩展表结构（已在库可单独执行）
USE pms;

ALTER TABLE sys_form_config
  ADD COLUMN api_list_path VARCHAR(256) DEFAULT NULL COMMENT '列表分页接口路径' AFTER biz_type,
  ADD COLUMN api_save_path VARCHAR(256) DEFAULT NULL COMMENT '保存接口基础路径' AFTER api_list_path;

ALTER TABLE sys_form_field
  ADD COLUMN show_in_list TINYINT DEFAULT 0 COMMENT '是否在列表中展示' AFTER sort_order,
  ADD COLUMN list_order INT DEFAULT 0 COMMENT '列表列排序' AFTER show_in_list,
  ADD COLUMN list_width VARCHAR(32) DEFAULT NULL COMMENT '列表列宽' AFTER list_order;
