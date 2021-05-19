package com.lewandowski.wycena3000.controller;

import com.lewandowski.wycena3000.entity.Project;
import com.lewandowski.wycena3000.entity.ProjectDetails;
import com.lewandowski.wycena3000.service.ProjectService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/creator/projects")
public class ProjectController {

    private final ProjectService projectService;

    public ProjectController(ProjectService projectService) {
        this.projectService = projectService;
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

        return "redirect:/creator/projects/all";
    }
}
