package com.pms.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class PmCompany {
    private Long id;
    private String companyCode;
    private String companyName;
    private Integer sortOrder;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

