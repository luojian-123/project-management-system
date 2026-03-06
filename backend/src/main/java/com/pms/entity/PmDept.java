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
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

