package com.pms.entity;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class PmIssue {
    private Long id;
    private Long projectId;
    private Long taskId;
    private String issueCode;
    private String title;
    private String description;
    private String severity;
    private String status;
    private Long assigneeId;
    private String assigneeName;
    private LocalDate foundDate;
    private LocalDate resolvedDate;
    private Long createdBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
