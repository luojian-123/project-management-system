package com.pms.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class PmRisk {
    private Long id;
    private Long projectId;
    private String riskCode;
    private String title;
    private String description;
    private String probability;
    private String impact;
    private String riskLevel;
    private String response;
    private Long ownerId;
    private String status;
    private Long createdBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
