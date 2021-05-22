package com.lewandowski.wycena3000.controller;

import com.lewandowski.wycena3000.entity.Board;
import com.lewandowski.wycena3000.entity.BoardType;
import com.lewandowski.wycena3000.service.BoardService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/creator/boards")
public class BoardController {

    private final BoardService boardService;

    public BoardController(BoardService boardService) {
        this.boardService = boardService;
    }

    @GetMapping("/add")
    public String add(Model model) {
        Board board = new Board();
        model.addAttribute("board", board);

        List<BoardType> boardTypes = boardService.getBoardTypes();
        model.addAttribute("boardTypes", boardTypes);

        return "board/board_add";
    }

    @PostMapping("/add")
    public String save(@ModelAttribute Board board) {
        boardService.save(board);

        return "redirect:/creator/boards/all";
    }

    @GetMapping("/edit")
    public String edit(@RequestParam long boardId, Model model) {
        Board board = boardService.findById(boardId);
        model.addAttribute("board", board);

        List<BoardType> boardTypes = boardService.getBoardTypes();
        model.addAttribute("boardTypes", boardTypes);

        return "board/board_edit";
    }


    @GetMapping("/all")
    public String all(Model model) {
        List<Board> boards = boardService.findAll();
        model.addAttribute("boards", boards);

        return "board/board_all";

    }


}
