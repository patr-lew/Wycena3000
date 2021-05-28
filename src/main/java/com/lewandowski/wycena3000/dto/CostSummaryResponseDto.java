package com.lewandowski.wycena3000.dto;

import com.lewandowski.wycena3000.entity.Board;
import com.lewandowski.wycena3000.entity.Part;
import com.lewandowski.wycena3000.entity.Project;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class CostSummaryResponseDto {

    private Project Project;

    private List<Board> boardsOfProject;

    private List<Part> partsOfProject;


}
