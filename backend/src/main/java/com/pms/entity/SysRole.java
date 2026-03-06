package com.pms.entity;

import lombok.Data;

@Data
public class SysRole {
    private Long id;
    private String code;
    private String name;
    private Integer status;
    private Long deptId;
    private Integer sortOrder;
    /** 关联的部门ID列表（多对多），不持久化到 sys_role 表，由 sys_role_dept 维护 */
    private java.util.List<Long> deptIds;
}
