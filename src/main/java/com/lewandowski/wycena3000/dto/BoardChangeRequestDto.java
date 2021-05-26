package com.lewandowski.wycena3000.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BoardChangeRequestDto {
    private Long projectId;
    private Long oldBoardId;
    private Long newBoardId;
}
