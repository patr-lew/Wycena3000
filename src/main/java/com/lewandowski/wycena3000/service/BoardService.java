package com.lewandowski.wycena3000.service;

import com.lewandowski.wycena3000.entity.Board;
import com.lewandowski.wycena3000.entity.BoardType;
import com.lewandowski.wycena3000.repository.BoardRepository;
import com.lewandowski.wycena3000.repository.BoardTypeRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BoardService {

    private final BoardRepository boardRepository;
    private final BoardTypeRepository boardTypeRepository;

    public BoardService(BoardRepository boardRepository, BoardTypeRepository boardTypeRepository) {
        this.boardRepository = boardRepository;
        this.boardTypeRepository = boardTypeRepository;
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




}
