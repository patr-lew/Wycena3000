package com.lewandowski.wycena3000.controller;

import com.lewandowski.wycena3000.dto.AddingPartDto;
import com.lewandowski.wycena3000.dto.BoardByProjectDto;
import com.lewandowski.wycena3000.dto.NewPriceRequestDto;
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
        ProjectDetails projectDetails = new ProjectDetails();
        projectDetails.setProject(project);
        projectService.saveProjectDetails(projectDetails);
        return "redirect:/creator/projects/edit/" + project.getId();
    }

    @GetMapping("/edit/{projectId}")
    public String editProject(@PathVariable long projectId,
                              @RequestParam(name = "boardId", required = false) Long lastAddedBoardId,
                              @RequestParam(required = false) boolean error,
                              Model model) {
        Project projectById = projectService.findByIdEager(projectId);
        String margin = projectService.marginToString(projectById);

        List<FurniturePart> furnitureParts = furniturePartService.getFurnitureParts();
        List<Board> boardsInProject = boardService.findAllByProjectId(projectId);
        List<FurniturePartType> partTypesInProject = furniturePartService.getFurniturePartTypesByProject(projectId);
        List<Board> boardsAll = boardService.findAll();
        BoardMeasurement boardMeasurement = new BoardMeasurement();

        model.addAttribute("error", error);
        model.addAttribute("project", projectById);
        model.addAttribute("boardId", lastAddedBoardId);
        model.addAttribute("furnitureParts", furnitureParts);
        model.addAttribute("boardsInProject", boardsInProject);
        model.addAttribute("partTypes", partTypesInProject);
        model.addAttribute("boards", boardsAll);
        model.addAttribute("board", boardMeasurement);
        model.addAttribute("projectMargin", margin);

        return "project/project_edit";

    }

    @GetMapping("/delete/{projectId}")
    public String delete(@PathVariable Long projectId) {
        projectService.delete(projectId);

        return "redirect:/creator/projects/all";
    }

    @PostMapping("/addFurniturePart")
    public String addFurniturePartToProject(@Valid AddingPartDto partDto, BindingResult result) {

        if (result.hasErrors()) {
            return "redirect:/creator/projects/edit/" + partDto.getProjectId() + "?error=true";
        }

        projectService.addFurniturePartsToProject(partDto);
        return "redirect:/creator/projects/edit/" + partDto.getProjectId();
    }

    @PostMapping("/calculatePrice")
    public String calculatePrice(@Valid NewPriceRequestDto newPriceRequestDto, BindingResult result) {

        if (result.hasErrors()) {
            return "redirect:/creator/projects/edit/" + newPriceRequestDto.getProjectId() + "?error=true";
        }

        if (newPriceRequestDto.getPrice() == null && newPriceRequestDto.getMargin() == null ||
            newPriceRequestDto.getPrice() != null && newPriceRequestDto.getMargin() != null) {
            return "redirect:/creator/projects/edit/" + newPriceRequestDto.getProjectId() + "?error=true";
        }

        projectService.setNewPrice(newPriceRequestDto);
        return "redirect:/creator/projects/edit/" + newPriceRequestDto.getProjectId();
    }

    @PostMapping("/addBoard")
    public String addBoardToProject(@RequestParam long projectId, @Valid BoardMeasurement boardMeasurement, BindingResult result) {

        if (result.hasErrors() || boardMeasurement.getAmount() < 1) {
            return "redirect:/creator/projects/edit/" + projectId + "?error=true";
        }

        projectService.addBoardMeasurementToProject(projectId, boardMeasurement);
        return "redirect:/creator/projects/edit/" + projectId +
                "?boardId=" + boardMeasurement.getBoard().getId();
    }

    @PostMapping("/addProjectDetails")
    public String addDetailsToProject(@RequestParam long projectId, @Valid ProjectDetails projectDetails, BindingResult result) {

        if (result.hasErrors()) {
            return "redirect:/creator/projects/edit/" + projectId + "?error=true";
        }

        projectService.addProjectDetailsToProject(projectId, projectDetails);
        return "redirect:/creator/projects/edit/" + projectId;
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
