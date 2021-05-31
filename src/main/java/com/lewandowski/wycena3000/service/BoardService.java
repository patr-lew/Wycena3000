package com.lewandowski.wycena3000.service;

import com.lewandowski.wycena3000.dto.BoardChangeRequestDto;
import com.lewandowski.wycena3000.entity.*;
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

        // updating BoardMeasurements in the project. If a measurement is duplicated,
        // move amount to new entry and set old one to 0
        for (Map.Entry<BoardMeasurement, Integer> measurementEntry : boardMeasurements.entrySet()) {
            BoardMeasurement currentMeasurement = measurementEntry.getKey();

            if (currentMeasurement.getBoard().getId() == changeDto.getOldBoardId()) {
                boolean isDoubled = false;

                for (Map.Entry<BoardMeasurement, Integer> sameMeasurementEntry : boardMeasurements.entrySet()) {
                    BoardMeasurement changedMeasurement = sameMeasurementEntry.getKey();

                    if (changedMeasurement.getBoard().equals(newBoard) &&
                            changedMeasurement.getHeight() == currentMeasurement.getHeight() &&
                            changedMeasurement.getWidth() == currentMeasurement.getWidth() &&
                            !changedMeasurement.equals(currentMeasurement)) {

                        int oldAmount = boardMeasurements.get(currentMeasurement);
                        int newAmount = boardMeasurements.get(changedMeasurement);

                        sameMeasurementEntry.setValue(oldAmount + newAmount);
                        measurementEntry.setValue(0);
                        isDoubled = true;
                    }
                }

                // change board only if there is no doubled entry, otherwise there is
                // repetition of primary keys in the table
                if (!isDoubled) {
                    currentMeasurement.setBoard(newBoard);
                }
            }
        }

        // removing entries with amount of board set to 0
        boardMeasurements.entrySet().removeIf(entry -> entry.getValue() == 0);

        projectRepository.save(project);
    }
}
