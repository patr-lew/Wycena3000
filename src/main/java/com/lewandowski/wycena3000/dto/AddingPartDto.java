package com.lewandowski.wycena3000.dto;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

@Getter
@Setter
public class AddingPartDto {
    @NotNull
    private Long projectId;

    @NotNull
    @Min(value = 1)
    private Long partId;

    @NotNull
    @Min(value = 1)
    private Integer amount;
}
