package com.lewandowski.wycena3000.service;

import com.lewandowski.wycena3000.dto.BoardChangeRequestDto;
import com.lewandowski.wycena3000.entity.*;
import com.lewandowski.wycena3000.repository.BoardMeasurementRepository;
import com.lewandowski.wycena3000.repository.BoardRepository;
import com.lewandowski.wycena3000.repository.BoardTypeRepository;
import com.lewandowski.wycena3000.repository.ProjectRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.persistence.EntityNotFoundException;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BoardService {
    private final BoardRepository boardRepository;
    private final BoardMeasurementRepository boardMeasurementRepository;
    private final BoardTypeRepository boardTypeRepository;
    private final ProjectRepository projectRepository;


    public List<Board> findAll() {
        return boardRepository.findAll();
    }

    public List<Board> findAllByUser(User user) {
        return boardRepository.findAllByUserIdOrderByIdAsc(user.getId());
    }

    public Board save(Board board) {
        return boardRepository.save(board);
    }

    public List<BoardType> getBoardTypes() {
        return boardTypeRepository.findAll();
    }


    public Board findById(long boardId) {
        return boardRepository
                .findById(boardId)
                .orElseThrow(() -> new EntityNotFoundException("Board with id '" + boardId + "' doesn't exist"));
    }

    public void delete(Long boardId) {
        Board boardToDelete = this.findById(boardId);
        boardRepository.delete(boardToDelete);
    }

    public Set<Long> getEnabledDeleteSet() {
        return boardRepository.enabledDeleteSet();
    }

    public List<Board> findAllByProjectId(long projectId) {
        return boardRepository.findAllByProjectId(projectId);
    }

    public void changeBoardInProject(BoardChangeRequestDto changeDto) {

        Project project = projectRepository.findById(changeDto.getProjectId())
                .orElseThrow(() -> new EntityNotFoundException("Project with id '" + changeDto.getProjectId() + "' doesn't exist"));

        Map<BoardMeasurement, Integer> boardMeasurements = project.getBoardMeasurements();
        Board newBoard = findById((changeDto.getNewBoardId()));

        Set<BoardMeasurement> measurementsToChange = getMeasurementsByBoard(boardMeasurements, changeDto.getOldBoardId());
        Set<BoardMeasurement> existingNewBoardMeasurements = getMeasurementsByBoard(boardMeasurements, changeDto.getNewBoardId());

        swapBoardsInMeasurements(
                boardMeasurements,
                measurementsToChange,
                existingNewBoardMeasurements,
                newBoard);

        projectRepository.save(project);
    }

    private void swapBoardsInMeasurements(Map<BoardMeasurement, Integer> boardMeasurements,
                                          Set<BoardMeasurement> measurementsToChange,
                                          Set<BoardMeasurement> existingNewBoardMeasurements,
                                          Board newBoard) {
        measurementsToChange.forEach(toChange -> {
            Optional<BoardMeasurement> doubledEntry = existingNewBoardMeasurements
                    .stream()
                    .filter(existing -> existing.getHeight() == toChange.getHeight()
                            && existing.getWidth() == toChange.getWidth())
                    .findAny();

            doubledEntry.ifPresentOrElse(
                    existing -> {
                        int oldAmount = boardMeasurements.get(toChange);
                        int newAmount = boardMeasurements.get(existing);

                        boardMeasurements.put(existing, oldAmount + newAmount);
                        boardMeasurements.remove(toChange);
                        boardMeasurementRepository.delete(toChange);
                    },
                    () -> toChange.setBoard(newBoard));
        });
    }

    private Set<BoardMeasurement> getMeasurementsByBoard(Map<BoardMeasurement, Integer> boardMeasurements, Long boardId) {
        return boardMeasurements.keySet().stream()
                .filter(measurement -> measurement.getBoard().getId() == boardId)
                .collect(Collectors.toSet());
    }
}
