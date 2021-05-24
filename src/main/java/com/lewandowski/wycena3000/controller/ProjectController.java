package com.lewandowski.wycena3000.controller;

import com.lewandowski.wycena3000.dto.AddingPartDto;
import com.lewandowski.wycena3000.dto.BoardByProjectDto;
import com.lewandowski.wycena3000.dto.PriceCalculationDto;
import com.lewandowski.wycena3000.entity.*;
import com.lewandowski.wycena3000.service.BoardService;
import com.lewandowski.wycena3000.service.FurniturePartService;
import com.lewandowski.wycena3000.service.ProjectService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
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
    public String addProject(@Valid Project project, BindingResult result) {

        if(result.hasErrors()) {
            return "project/projects_add";
        }

        projectService.save(project);
        return "redirect:/creator/projects/edit?projectId=" + project.getId();
    }

    @GetMapping("/edit")
    public String editProject(@RequestParam long projectId,
                              @RequestParam(required = false) Long boardId,
                              @RequestParam(required = false) boolean error,
                              Model model) {
        Project projectById = projectService.findByIdEager(projectId);
        String margin = projectService.computeMargin(projectById);

        if (null == projectById.getProjectDetails()) {
            projectById.setProjectDetails(new ProjectDetails());
        }

        List<FurniturePart> furnitureParts = furniturePartService.getFurnitureParts();
        List<Board> boardsInProject = boardService.findAllByProjectId(projectId);
        List<FurniturePartType> partTypesInProject = furniturePartService.getFurniturePartTypesByProject(projectId);
        List<Board> boardsAll = boardService.findAll();
        BoardMeasurement boardMeasurement = new BoardMeasurement();

        model.addAttribute("error", error);
        model.addAttribute("project", projectById);
        model.addAttribute("boardId", boardId);
        model.addAttribute("furnitureParts", furnitureParts);
        model.addAttribute("boardsInProject", boardsInProject);
        model.addAttribute("partTypes", partTypesInProject);
        model.addAttribute("boards", boardsAll);
        model.addAttribute("board", boardMeasurement);
        model.addAttribute("projectMargin", margin);

        return "project/project_edit";

    }

    @PostMapping("/addFurniturePart")
    public String addFurniturePartToProject(@Valid AddingPartDto partDto, BindingResult result) {

        if (result.hasErrors()) {
            return "redirect:/creator/projects/edit?projectId=" + partDto.getProjectId() + "&error=true";
        }

        projectService.addFurniturePartsToProject(partDto);
        return "redirect:/creator/projects/edit?projectId=" + partDto.getProjectId();
    }

    @PostMapping("/calculatePrice")
    public String calculatePrice(@Valid PriceCalculationDto priceCalculationDto, BindingResult result) {

        if (result.hasErrors()) {
            return "redirect:/creator/projects/edit?projectId=" + priceCalculationDto.getProjectId() + "&error=true";
        }

        if (priceCalculationDto.getPrice() == null && priceCalculationDto.getMargin() == null ||
            priceCalculationDto.getPrice() != null && priceCalculationDto.getMargin() != null) {
            return "redirect:/creator/projects/edit?projectId=" + priceCalculationDto.getProjectId() + "&error=true";
        }

        projectService.setNewPrice(priceCalculationDto);
        return "redirect:/creator/projects/edit?projectId=" + priceCalculationDto.getProjectId();
    }

    @PostMapping("/addBoard")
    public String addBoardToProject(@RequestParam long projectId, @Valid BoardMeasurement boardMeasurement, BindingResult result) {

        if (result.hasErrors()) {
            return "redirect:/creator/projects/edit?projectId=" + projectId + "&error=true";
        }

        projectService.addBoardMeasurementToProject(projectId, boardMeasurement);
        return "redirect:/creator/projects/edit?projectId=" + projectId +
                "&boardId=" + boardMeasurement.getBoard().getId();
    }

    @PostMapping("/addProjectDetails")
    public String addDetailsToProject(@RequestParam long projectId, @Valid ProjectDetails projectDetails, BindingResult result) {

        if (result.hasErrors()) {
            return "redirect:/creator/projects/edit?projectId=" + projectId + "&error=true";
        }

        projectService.addProjectDetailsToProject(projectId, projectDetails);
        return "redirect:/creator/projects/edit?projectId=" + projectId;
    }

    @GetMapping("/details/{projectId}")
    public String projectDetails(@PathVariable Long projectId, Model model) {
        Project projectById = projectService.findByIdEager(projectId);
        model.addAttribute("project", projectById);

        List<BoardByProjectDto> boards = projectService.getBoardsDetailsByProject(projectId);
        model.addAttribute("boardsInProject", boards);

        List<FurniturePartType> furniturePartTypes = furniturePartService.getFurniturePartTypes();
        model.addAttribute("partTypes", furniturePartTypes);

        return "project/project_details";
    }
}
