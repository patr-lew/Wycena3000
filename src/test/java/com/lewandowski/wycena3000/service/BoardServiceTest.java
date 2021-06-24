package com.lewandowski.wycena3000.service;

import com.lewandowski.wycena3000.dto.BoardChangeRequestDto;
import com.lewandowski.wycena3000.entity.Board;
import com.lewandowski.wycena3000.entity.Measurement;
import com.lewandowski.wycena3000.entity.Project;
import com.lewandowski.wycena3000.repository.BoardRepository;
import com.lewandowski.wycena3000.repository.BoardTypeRepository;
import com.lewandowski.wycena3000.repository.MeasurementRepository;
import com.lewandowski.wycena3000.repository.ProjectRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashMap;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BoardServiceTest {
    @Mock
    private BoardRepository boardRepository;
    @Mock
    private BoardTypeRepository boardTypeRepository;
    @Mock
    private ProjectRepository projectRepository;
    @Mock
    private MeasurementRepository measurementRepository;

    @InjectMocks
    private BoardService boardService;


    @Test
    public void whenProjectSwitchesBoardToNewBoard_thenBoardIsChanged() {
        // given
        final Long projectId = 1L;
        final Long oldBoardId = 5L;
        final Long newBoardId = 10L;
        final Integer amountOfBoards = 15;

        Board oldBoard = new Board();
        oldBoard.setId(oldBoardId);
        oldBoard.setName("Old board"); // hash doesn't include Id

        Board newBoard = new Board();
        newBoard.setId(newBoardId);
        newBoard.setName("New board");

        Measurement measurement = new Measurement();
        measurement.setBoard(oldBoard);

        Project testProject = new Project();
        testProject.setMeasurements(new HashMap<>());
        testProject.getMeasurements().put(measurement, amountOfBoards);

        BoardChangeRequestDto dto = new BoardChangeRequestDto(projectId, oldBoardId, newBoardId);

        when(projectRepository.findById(projectId)).thenReturn(Optional.of(testProject));
        when(boardRepository.findById(newBoardId)).thenReturn(Optional.of(newBoard));

        // when
        boardService.changeBoardInProject(dto);

        // then
        Measurement changedMeasurement = testProject.getMeasurements().keySet()
                .stream()
                .findFirst()
                .get();
        assertThat(changedMeasurement.getBoard()).isEqualTo(newBoard);

    }

    @Test
    public void whenProjectSwitchesBoardToExistingBoard_thenBoardIsUpdated() {
        // given
        final Long projectId = 1L;
        final Long oldBoardId = 5L;
        final Long newBoardId = 10L;
        final Integer amountOfOldBoards = 15;
        final Integer amountOfNewBoards = 25;

        Board oldBoard = new Board();
        oldBoard.setId(oldBoardId);
        oldBoard.setName("Old board"); // hash doesn't include Id

        Board newBoard = new Board();
        newBoard.setId(newBoardId);
        newBoard.setName("New board");

        Measurement existingMeasurement = new Measurement();
        existingMeasurement.setBoard(newBoard);

        Measurement toBeUpdatedMeasurement = new Measurement();
        toBeUpdatedMeasurement.setBoard(oldBoard);

        Project testProject = new Project();
        testProject.setMeasurements(new HashMap<>());
        testProject.getMeasurements().put(existingMeasurement, amountOfNewBoards);
        testProject.getMeasurements().put(toBeUpdatedMeasurement, amountOfOldBoards);

        BoardChangeRequestDto dto = new BoardChangeRequestDto(projectId, oldBoardId, newBoardId);

        when(projectRepository.findById(projectId)).thenReturn(Optional.of(testProject));
        when(boardRepository.findById(newBoardId)).thenReturn(Optional.of(newBoard));

        // when
        boardService.changeBoardInProject(dto);

        // then
        assertThat(testProject.getMeasurements().size()).isEqualTo(1);

        Measurement changedMeasurement
                = testProject.getMeasurements().keySet()
                .stream()
                .findFirst()
                .get();
        assertThat(changedMeasurement.getBoard()).isEqualTo(newBoard);

        Integer calculatedAmount
                = testProject.getMeasurements().values()
                .stream()
                .findFirst()
                .get();
        assertThat(calculatedAmount).isEqualTo(amountOfNewBoards + amountOfOldBoards);

    }


}