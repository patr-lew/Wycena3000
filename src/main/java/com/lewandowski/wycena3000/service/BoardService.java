package com.lewandowski.wycena3000.service;

import com.lewandowski.wycena3000.dto.BoardChangeRequestDto;
import com.lewandowski.wycena3000.entity.*;
import com.lewandowski.wycena3000.repository.BoardRepository;
import com.lewandowski.wycena3000.repository.BoardTypeRepository;
import com.lewandowski.wycena3000.repository.MeasurementRepository;
import com.lewandowski.wycena3000.repository.ProjectRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.persistence.EntityNotFoundException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BoardService {
    private final BoardRepository boardRepository;
    private final MeasurementRepository measurementRepository;
    private final BoardTypeRepository boardTypeRepository;
    private final ProjectRepository projectRepository;

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

        Project project = projectRepository
                .findById(changeDto.getProjectId())
                .orElseThrow(() -> new EntityNotFoundException("Project with id '" + changeDto.getProjectId() + "' doesn't exist"));

        Map<Measurement, Integer> measurements = project.getMeasurements();
        Board newBoard = findById((changeDto.getNewBoardId()));

        Set<Measurement> measurementsToChange = getMeasurementsByBoard(measurements, changeDto.getOldBoardId());
        Set<Measurement> existingNewMeasurements = getMeasurementsByBoard(measurements, changeDto.getNewBoardId());

        swapBoardsInMeasurements(
                measurements,
                measurementsToChange,
                existingNewMeasurements,
                newBoard);

        projectRepository.save(project);
    }

    private void swapBoardsInMeasurements(Map<Measurement, Integer> measurements,
                                          Set<Measurement> measurementsToChange,
                                          Set<Measurement> existingNewMeasurements,
                                          Board newBoard) {
        measurementsToChange.forEach(toChange -> {
            Optional<Measurement> doubledEntry = existingNewMeasurements
                    .stream()
                    .filter(existing ->
                            existing.getHeight() == toChange.getHeight() &&
                                    existing.getWidth() == toChange.getWidth())
                    .findAny();

            doubledEntry.ifPresentOrElse(
                    existing -> {
                        int oldAmount = measurements.get(toChange);
                        int newAmount = measurements.get(existing);

                        measurements.put(existing, oldAmount + newAmount);
                        measurements.remove(toChange);
                        measurementRepository.delete(toChange);
                    },
                    () -> toChange.setBoard(newBoard));
        });
    }

    private Set<Measurement> getMeasurementsByBoard(Map<Measurement, Integer> measurements, Long boardId) {
        return measurements.keySet().stream()
                .filter(measurement -> measurement.getBoard().getId() == boardId)
                .collect(Collectors.toSet());
    }
}
