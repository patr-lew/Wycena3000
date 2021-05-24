package com.lewandowski.wycena3000.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AddingPartDto {
    private Long projectId;
    private Long furniturePartId;
    private Integer amount;
}
