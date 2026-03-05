package com.pms.entity;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class PmProject {
    private Long id;
    private String projectCode;
    private String projectName;
    private String description;
    private Long ownerId;
    private String ownerName;
    private String customerName;
    private LocalDate planStart;
    private LocalDate planEnd;
    private LocalDate actualStart;
    private LocalDate actualEnd;
    private String status;
    private Long createdBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
