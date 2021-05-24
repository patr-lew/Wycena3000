package com.lewandowski.wycena3000.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter @Setter
public class PriceCalculationDto {
    private Long projectId;
    private BigDecimal price;
    private Integer margin;

}
