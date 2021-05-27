package com.lewandowski.wycena3000.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PartChangeRequestDto {
    private Long projectId;
    private Long oldPartId;
    private Long newPartId;


}
