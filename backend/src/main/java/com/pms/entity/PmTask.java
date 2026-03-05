package com.pms.entity;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class PmTask {
    private Long id;
    private Long projectId;
    private Long parentId;
    private String taskCode;
    private String taskName;
    private Long assigneeId;
    private String assigneeName;
    private String projectName;
    private LocalDate planStart;
    private LocalDate planEnd;
    private LocalDate actualStart;
    private LocalDate actualEnd;
    private Integer progress;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    /** 子任务（树形时使用，非数据库字段） */
    private List<PmTask> children;
}
