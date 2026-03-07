package com.pms.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class PmCompany {
    private Long id;
    private String companyCode;
    private String companyName;
    private Integer sortOrder;
    /** 是否系统公司（默认顶层，不可删除） */
    private Boolean isSystem;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

