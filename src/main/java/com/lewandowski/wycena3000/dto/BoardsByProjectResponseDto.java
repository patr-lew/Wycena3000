package com.lewandowski.wycena3000.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class BoardsByProjectResponseDto {
    private long boardId;
    private String name;
    private BigDecimal totalArea = BigDecimal.ZERO;
    private BigDecimal totalCost = BigDecimal.ZERO;
}
