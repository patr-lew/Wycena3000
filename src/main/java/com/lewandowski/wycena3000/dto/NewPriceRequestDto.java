package com.lewandowski.wycena3000.dto;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.Min;
import java.math.BigDecimal;

@Getter @Setter
public class NewPriceRequestDto {

    private Long projectId;

    @DecimalMin(value = "0.01")
    private BigDecimal price;

    @Min(value = 1)
    private Integer margin;

}
