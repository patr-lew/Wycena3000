package com.lewandowski.wycena3000.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BoardChangeRequestDto {
    private Long projectId;
    private Long oldBoardId;
    private Long newBoardId;
}
