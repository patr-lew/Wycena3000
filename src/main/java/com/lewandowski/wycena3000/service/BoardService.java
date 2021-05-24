package com.lewandowski.wycena3000.service;

import com.lewandowski.wycena3000.dto.BoardChangeDto;
import com.lewandowski.wycena3000.entity.Board;
import com.lewandowski.wycena3000.entity.BoardMeasurement;
import com.lewandowski.wycena3000.entity.BoardType;
import com.lewandowski.wycena3000.repository.BoardMeasurementRepository;
import com.lewandowski.wycena3000.repository.BoardRepository;
import com.lewandowski.wycena3000.repository.BoardTypeRepository;
import org.springframework.stereotype.Service;

import javax.persistence.EntityNotFoundException;
import javax.transaction.Transactional;
import java.util.List;

@Service
@Transactional
public class BoardService {

    private final BoardRepository boardRepository;
    private final BoardTypeRepository boardTypeRepository;
    private final BoardMeasurementRepository boardMeasurementRepository;

    public BoardService(BoardRepository boardRepository, BoardTypeRepository boardTypeRepository, BoardMeasurementRepository boardMeasurementRepository) {
        this.boardRepository = boardRepository;
        this.boardTypeRepository = boardTypeRepository;
        this.boardMeasurementRepository = boardMeasurementRepository;
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

    public void changeBoardInProject(BoardChangeDto changeDto) {

        Board newBoard = findById(changeDto.getNewBoardId());


        List<BoardMeasurement> boardMeasurements = boardMeasurementRepository.findAllByProjectId(changeDto.getProjectId());

        boardMeasurements.forEach(measurement -> {
            if (measurement.getBoard().getId() == changeDto.getOldBoardId()) {
                measurement.setBoard(newBoard);
                boardMeasurementRepository.save(measurement);
            }
        });


    }
}
