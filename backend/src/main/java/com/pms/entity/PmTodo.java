package com.pms.entity;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class PmTodo {
    private Long id;
    private Long userId;
    private String title;
    private String bizType;
    private Long bizId;
    private String priority;
    private LocalDate dueDate;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
