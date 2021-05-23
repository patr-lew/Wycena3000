package com.lewandowski.wycena3000.controller;

import com.lewandowski.wycena3000.entity.*;
import com.lewandowski.wycena3000.service.BoardService;
import com.lewandowski.wycena3000.service.FurniturePartService;
import com.lewandowski.wycena3000.service.ProjectService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@Controller
@RequestMapping("/creator/projects")
public class ProjectController {

    private final ProjectService projectService;
    private final FurniturePartService furniturePartService;
    private final BoardService boardService;

    public ProjectController(ProjectService projectService, FurniturePartService furniturePartService, BoardService boardService) {
        this.projectService = projectService;
        this.furniturePartService = furniturePartService;
        this.boardService = boardService;
    }

    @GetMapping("/all")
    public String findAll(Model model) {
        List<Project> projects = projectService.findAll();

        // pass computed margin (price/cost) to the view
        List<String> margins = projectService.computeMarginList(projects);

        model.addAttribute("projects", projects);
        model.addAttribute("margins", margins);

        return "project/projects_all";
    }

    @GetMapping("/add")
    public String addProject(Model model) {
        Project project = new Project();

        model.addAttribute("project", project);

        return "project/projects_add";
    }

    @PostMapping("/add")
    public String addProject(@ModelAttribute Project project) {
        projectService.save(project);

        return "redirect:/creator/projects/edit?projectId=" + project.getId();
    }

    @GetMapping("/edit")
    public String editProject(@RequestParam long projectId,
                              @RequestParam(required = false) Long boardId, Model model) {
        Project projectById = projectService.findByIdEager(projectId);

        if (null == projectById.getProjectDetails()) {
            projectById.setProjectDetails(new ProjectDetails());
        }

        model.addAttribute("project", projectById);
        model.addAttribute("boardId", boardId);

        List<FurniturePart> furnitureParts = furniturePartService.getFurnitureParts();
        model.addAttribute("furnitureParts", furnitureParts);

        List<Board> boardsInProject = boardService.findAllByProjectId(projectId);
        model.addAttribute("boardsInProject", boardsInProject);

        List<FurniturePartType> partTypesInProject = furniturePartService.getFurniturePartTypesByProject(projectId);
        model.addAttribute("partTypes", partTypesInProject);

        List<Board> boardsAll = boardService.findAll();
        model.addAttribute("boards", boardsAll);
        BoardMeasurement boardMeasurement = new BoardMeasurement();
        model.addAttribute("board", boardMeasurement);

        return "project/project_edit";

    }

    @PostMapping("/addFurniturePart")
    public String addFurniturePartToProject(
            @RequestParam long projectId,
            @RequestParam long furniturePartId,
            @RequestParam int amount) {
        Project projectById = projectService.findById(projectId);

        projectService.addFurniturePartsToProject(projectById, furniturePartId, amount);
        return "redirect:/creator/projects/edit?projectId=" + projectById.getId();

    }

    @PostMapping("/addBoard")
    public String addBoardToProject(@RequestParam long projectId, @ModelAttribute BoardMeasurement boardMeasurement) {
        Project projectById = projectService.findById(projectId);

        projectService.addBoardMeasurementToProject(projectById, boardMeasurement);

        return "redirect:/creator/projects/edit?projectId=" + projectById.getId() +
                "&boardId=" + boardMeasurement.getBoard().getId();
    }

    @PostMapping("/addProjectDetails")
    public String addDetailsToProject(@RequestParam long projectId, @ModelAttribute ProjectDetails projectDetails) {

        projectService.addProjectDetailsToProject(projectId, projectDetails);


        return "redirect:/creator/projects/edit?projectId=" + projectId;
    }
}
