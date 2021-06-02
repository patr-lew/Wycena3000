package com.lewandowski.wycena3000.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PartChangeRequestDto {
    private Long projectId;
    private Long oldPartId;
    private Long newPartId;


}
