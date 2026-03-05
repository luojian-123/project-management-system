package com.pms.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class PmChange {
    private Long id;
    private Long projectId;
    private String changeNo;
    private String title;
    private String description;
    private String changeType;
    private String impactScope;
    private String status;
    private Long applicantId;
    private LocalDateTime applyTime;
    private Long flowId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
