package com.lewandowski.wycena3000.service;

import com.lewandowski.wycena3000.dto.BoardChangeRequestDto;
import com.lewandowski.wycena3000.entity.Board;
import com.lewandowski.wycena3000.entity.BoardMeasurement;
import com.lewandowski.wycena3000.entity.Project;
import com.lewandowski.wycena3000.repository.BoardMeasurementRepository;
import com.lewandowski.wycena3000.repository.BoardRepository;
import com.lewandowski.wycena3000.repository.BoardTypeRepository;
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
    private BoardMeasurementRepository boardMeasurementRepository;

    @InjectMocks
    private BoardService boardService;


    @Test
    public void whenProjectSwitchesBoardToNewBoard_thenBoardIsChanged() {
        // given
        final Long PROJECT_ID = 1L;
        final Long OLD_BOARD_ID = 5L;
        final Long NEW_BOARD_ID = 10L;
        final Integer AMOUNT_OF_BOARDS = 15;

        Board oldBoard = new Board();
        oldBoard.setId(OLD_BOARD_ID);
        oldBoard.setName("Old board"); // hash doesn't include Id

        Board newBoard = new Board();
        newBoard.setId(NEW_BOARD_ID);
        newBoard.setName("New board");

        BoardMeasurement measurement = new BoardMeasurement();
        measurement.setBoard(oldBoard);

        Project testProject = new Project();
        testProject.setBoardMeasurements(new HashMap<>());
        testProject.getBoardMeasurements().put(measurement, AMOUNT_OF_BOARDS);

        BoardChangeRequestDto dto = new BoardChangeRequestDto(PROJECT_ID, OLD_BOARD_ID, NEW_BOARD_ID);

        // when
        when(projectRepository.findById(PROJECT_ID)).thenReturn(Optional.of(testProject));
        when(boardRepository.findById(NEW_BOARD_ID)).thenReturn(Optional.of(newBoard));


        boardService.changeBoardInProject(dto);

        // then
        BoardMeasurement changedMeasurement = testProject.getBoardMeasurements().keySet()
                .stream()
                .findFirst()
                .get();
        assertThat(changedMeasurement.getBoard()).isEqualTo(newBoard);

    }

    @Test
    public void whenProjectSwitchesBoardToExistingBoard_thenBoardIsUpdated() {
        // given
        final Long PROJECT_ID = 1L;
        final Long OLD_BOARD_ID = 5L;
        final Long NEW_BOARD_ID = 10L;
        final Integer AMOUNT_OF_OLD_BOARDS = 15;
        final Integer AMOUNT_OF_NEW_BOARDS = 25;

        Board oldBoard = new Board();
        oldBoard.setId(OLD_BOARD_ID);
        oldBoard.setName("Old board"); // hash doesn't include Id

        Board newBoard = new Board();
        newBoard.setId(NEW_BOARD_ID);
        newBoard.setName("New board");

        BoardMeasurement existingMeasurement = new BoardMeasurement();
        existingMeasurement.setBoard(newBoard);

        BoardMeasurement toBeUpdatedMeasurement = new BoardMeasurement();
        toBeUpdatedMeasurement.setBoard(oldBoard);

        Project testProject = new Project();
        testProject.setBoardMeasurements(new HashMap<>());
        testProject.getBoardMeasurements().put(existingMeasurement, AMOUNT_OF_NEW_BOARDS);
        testProject.getBoardMeasurements().put(toBeUpdatedMeasurement, AMOUNT_OF_OLD_BOARDS);

        BoardChangeRequestDto dto = new BoardChangeRequestDto(PROJECT_ID, OLD_BOARD_ID, NEW_BOARD_ID);

        // when
        when(projectRepository.findById(PROJECT_ID)).thenReturn(Optional.of(testProject));
        when(boardRepository.findById(NEW_BOARD_ID)).thenReturn(Optional.of(newBoard));


        boardService.changeBoardInProject(dto);

        // then
        assertThat(testProject.getBoardMeasurements().size()).isEqualTo(1);

        BoardMeasurement changedMeasurement
                = testProject.getBoardMeasurements().keySet()
                .stream()
                .findFirst()
                .get();
        assertThat(changedMeasurement.getBoard()).isEqualTo(newBoard);

        Integer calculatedAmount
                = testProject.getBoardMeasurements().values()
                .stream()
                .findFirst()
                .get();
        assertThat(calculatedAmount).isEqualTo(AMOUNT_OF_NEW_BOARDS + AMOUNT_OF_OLD_BOARDS);

    }




}