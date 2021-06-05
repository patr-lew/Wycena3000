package com.lewandowski.wycena3000.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AddPartToProjectRequestDto {
    @NotNull
    private Long projectId;

    @NotNull
    @Min(value = 1)
    private Long partId;

    @NotNull
    private Integer amount;
}
