package com.lewandowski.wycena3000.controller;

import com.lewandowski.wycena3000.dto.AddingPartDto;
import com.lewandowski.wycena3000.dto.BoardByProjectDto;
import com.lewandowski.wycena3000.dto.NewPriceRequestDto;
import com.lewandowski.wycena3000.entity.*;
import com.lewandowski.wycena3000.security.CurrentUser;
import com.lewandowski.wycena3000.service.BoardService;
import com.lewandowski.wycena3000.service.PartService;
import com.lewandowski.wycena3000.service.ProjectService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.Set;

@Controller
@RequestMapping("/creator/projects")
public class ProjectController {

    private final ProjectService projectService;
    private final PartService partService;
    private final BoardService boardService;

    public ProjectController(ProjectService projectService, PartService partService, BoardService boardService) {
        this.projectService = projectService;
        this.partService = partService;
        this.boardService = boardService;
    }

    @GetMapping("/all")
    public String findAll(Model model, @AuthenticationPrincipal CurrentUser currentUser) {
        List<Project> projects = projectService.findAllByUserId(currentUser.getUser());

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
    public String addProject(@Valid Project project, BindingResult result, @AuthenticationPrincipal CurrentUser currentUser) {

        if(result.hasErrors()) {
            return "project/projects_add";
        }
        project.setUser(currentUser.getUser());

        ProjectDetails projectDetails = new ProjectDetails();
        projectDetails.setProject(project);
        projectService.saveProjectDetails(projectDetails);
        return "redirect:/creator/projects/edit/" + project.getId();
    }

    @GetMapping("/details/{projectId}")
    public String projectDetails(@PathVariable Long projectId, Model model, @AuthenticationPrincipal CurrentUser currentUser) {
        Project projectById = projectService.findByIdEager(projectId);
        if(projectById.getUser().getId() != currentUser.getUser().getId()) {
            return "redirect:/creator/projects/all"; // todo add 403 screen
        }
        model.addAttribute("project", projectById);

        List<BoardByProjectDto> boards = projectService.getBoardsDetailsByProject(projectId);
        model.addAttribute("boardsInProject", boards);

        List<PartType> partTypes = partService.getPartTypesByProject(projectId);
        model.addAttribute("partTypes", partTypes);

        return "project/project_details";
    }

    @GetMapping("/edit/{projectId}")
    public String editProject(@PathVariable long projectId,
                              @RequestParam(name = "boardId", required = false) Long lastAddedBoardId,
                              @RequestParam(required = false) boolean error,
                              Model model,
                              @AuthenticationPrincipal CurrentUser currentUser) {
        Project projectById = projectService.findByIdEager(projectId);
        if(projectById.getUser().getId() != currentUser.getUser().getId()) {
            return "redirect:/creator/projects/all"; // todo add 403 screen
        }
        String margin = projectService.marginToString(projectById);

        List<Part> parts = partService.getPartsByUser(currentUser.getUser());
        List<Board> boardsInProject = boardService.findAllByProjectId(projectId);
        List<PartType> partTypesInProject = partService.getPartTypesByProject(projectId);
        List<Board> boardsAll = boardService.findAllByUser(currentUser.getUser());
        BoardMeasurement boardMeasurement = new BoardMeasurement();

        model.addAttribute("error", error);
        model.addAttribute("project", projectById);
        model.addAttribute("boardId", lastAddedBoardId);
        model.addAttribute("parts", parts);
        model.addAttribute("boardsInProject", boardsInProject);
        model.addAttribute("partTypes", partTypesInProject);
        model.addAttribute("boards", boardsAll);
        model.addAttribute("board", boardMeasurement);
        model.addAttribute("projectMargin", margin);

        return "project/project_edit";

    }

    @GetMapping("/delete/{projectId}")
    public String delete(@PathVariable Long projectId,
                         @AuthenticationPrincipal CurrentUser currentUser) {
        Project projectById = projectService.findById(projectId);
        if(projectById.getUser().getId() != currentUser.getUser().getId()) {
            return "redirect:/creator/projects/all"; // todo add 403 screen
        }
        projectService.delete(projectId);

        return "redirect:/creator/projects/all";
    }

    @PostMapping("/addPart")
    public String addPartToProject(@Valid AddingPartDto partDto, BindingResult result) {

        if (result.hasErrors()) {
            return "redirect:/creator/projects/edit/" + partDto.getProjectId() + "?error=true";
        }

        projectService.addPartToProject(partDto);
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


}
