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
}
