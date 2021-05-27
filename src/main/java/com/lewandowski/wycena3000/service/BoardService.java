package com.lewandowski.wycena3000.service;

import com.lewandowski.wycena3000.dto.BoardChangeRequestDto;
import com.lewandowski.wycena3000.entity.Board;
import com.lewandowski.wycena3000.entity.BoardMeasurement;
import com.lewandowski.wycena3000.entity.BoardType;
import com.lewandowski.wycena3000.entity.Project;
import com.lewandowski.wycena3000.repository.BoardMeasurementRepository;
import com.lewandowski.wycena3000.repository.BoardRepository;
import com.lewandowski.wycena3000.repository.BoardTypeRepository;
import com.lewandowski.wycena3000.repository.ProjectRepository;
import org.springframework.stereotype.Service;

import javax.persistence.EntityNotFoundException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class BoardService {

    private final BoardRepository boardRepository;
    private final BoardTypeRepository boardTypeRepository;
    private final BoardMeasurementRepository boardMeasurementRepository;
    private final ProjectRepository projectRepository;

    public BoardService(BoardRepository boardRepository, BoardTypeRepository boardTypeRepository, BoardMeasurementRepository boardMeasurementRepository, ProjectRepository projectRepository) {
        this.boardRepository = boardRepository;
        this.boardTypeRepository = boardTypeRepository;
        this.boardMeasurementRepository = boardMeasurementRepository;
        this.projectRepository = projectRepository;
    }

    public List<Board> findAll() {
        return boardRepository.findAll();
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

    public List<Board> findAllByProjectId(long projectId) {
        return boardRepository.findAllByProjectId(projectId);
    }

    public void changeBoardInProject(BoardChangeRequestDto changeDto) {

        Project project = projectRepository.findById(changeDto.getProjectId())
                .orElseThrow(() -> new EntityNotFoundException("Project with id '" + changeDto.getProjectId() + "' doesn't exist"));

        Map<BoardMeasurement, Integer> boardMeasurements = project.getBoardMeasurements();
        Board newBoard = findById((changeDto.getNewBoardId()));

        // updating BoardMeasurements in the project. If a measurement is duplicated,
        // move amount to new entry and set old one to 0
        for (Map.Entry<BoardMeasurement, Integer> measurementEntry : boardMeasurements.entrySet()) {
            BoardMeasurement measurement = measurementEntry.getKey();

            if (measurement.getBoard().getId() == changeDto.getOldBoardId()) {
                int firstAmount = boardMeasurements.get(measurement);
                boolean isDoubled = false;

                for (Map.Entry<BoardMeasurement, Integer> sameMeasurementEntry : boardMeasurements.entrySet()) {
                    BoardMeasurement changedMeasurement = sameMeasurementEntry.getKey();

                    if (changedMeasurement.getBoard().equals(newBoard) &&
                            changedMeasurement.getHeight() == measurement.getHeight() &&
                            changedMeasurement.getWidth() == measurement.getWidth() &&
                            !changedMeasurement.equals(measurement)) {

                        int secondAmount = boardMeasurements.get(changedMeasurement);

                        sameMeasurementEntry.setValue(firstAmount + secondAmount);
                        measurementEntry.setValue(0);
                        isDoubled = true;
                    }
                }

                // change board only if there is no doubled entry, otherwise there is
                // repetition of primary keys in the table
                if (!isDoubled) {
                    measurement.setBoard(newBoard);
                }
            }
        }

        // removing entries with amount of board set to 0
        Set<Map.Entry<BoardMeasurement, Integer>> entrySet = boardMeasurements.entrySet();
        Iterator<Map.Entry<BoardMeasurement, Integer>> iterator = entrySet.iterator();

        while (iterator.hasNext()) {
            Map.Entry<BoardMeasurement, Integer> entry = iterator.next();
            if (entry.getValue() == 0) {
                iterator.remove();
            }
        }

        projectRepository.save(project);

    }
}
