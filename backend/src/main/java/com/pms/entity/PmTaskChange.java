package com.pms.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class PmTaskChange {
    private Long id;
    private Long taskId;
    private Long operatorId;
    private String operatorName;
    private String action;
    private String content;
    private LocalDateTime createdAt;
}
