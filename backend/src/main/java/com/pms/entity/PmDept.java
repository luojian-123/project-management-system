package com.pms.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class PmDept {
    private Long id;
    private Long companyId;
    private String deptCode;
    private String deptName;
    private Integer sortOrder;
    /** 是否系统部门（不可删除，管理员默认所属） */
    private Boolean isSystem;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

