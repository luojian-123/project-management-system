package com.pms.entity;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class PmCost {
    private Long id;
    private Long projectId;
    private Long taskId;
    private String costType;
    private BigDecimal budgetAmount;
    private BigDecimal actualAmount;
    private LocalDate occurDate;
    private String remark;
    private Long createdBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
