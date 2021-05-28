package com.lewandowski.wycena3000.controller;

import com.lewandowski.wycena3000.dto.BoardChangeRequestDto;
import com.lewandowski.wycena3000.entity.Board;
import com.lewandowski.wycena3000.entity.BoardType;
import com.lewandowski.wycena3000.entity.Project;
import com.lewandowski.wycena3000.service.BoardService;
import com.lewandowski.wycena3000.service.ProjectService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.Set;

@Controller
@RequestMapping("/creator/boards")
public class BoardController {

    private final BoardService boardService;
    private final ProjectService projectService;

    public BoardController(BoardService boardService, ProjectService projectService) {
        this.boardService = boardService;
        this.projectService = projectService;
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
    public String save(@Valid Board board, BindingResult result, Model model) {
        if(result.hasErrors()) {
            List<BoardType> boardTypes = boardService.getBoardTypes();
            model.addAttribute("boardTypes", boardTypes);

            return "board/board_add";
        }

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

    @GetMapping("/delete/{boardId}")
    public String delete(@PathVariable Long boardId) {
        boardService.delete(boardId);

        return "redirect:/creator/boards/all";
    }


    @GetMapping("/all")
    public String all(Model model) {
        List<Board> boards = boardService.findAll();
        Set<Long> enabledDeleteSet = boardService.getEnabledDeleteSet();
        model.addAttribute("boards", boards);
        model.addAttribute("enabledDelete", enabledDeleteSet);

        return "board/board_all";

    }

    @GetMapping("/change")
    public String changeBoard(@RequestParam Long boardId,
                              @RequestParam Long projectId, Model model) {
        Project project = projectService.findById(projectId);
        List<Board> boards = boardService.findAll();

        model.addAttribute("project", project);
        model.addAttribute("oldBoardId", boardId);
        model.addAttribute("boards", boards);
        return "board/board_change";
    }

    @PostMapping("/change")
    public String changeBoard(@ModelAttribute BoardChangeRequestDto dto) {
        boardService.changeBoardInProject(dto);

        return "redirect:/creator/projects/details/" + dto.getProjectId();
    }


}
