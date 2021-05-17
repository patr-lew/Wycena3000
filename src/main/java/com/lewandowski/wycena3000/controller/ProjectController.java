package com.lewandowski.wycena3000.controller;

import com.lewandowski.wycena3000.service.ProjectService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/creator/project")
public class ProjectController {

    private final ProjectService projectService;

    public ProjectController(ProjectService projectService) {
        this.projectService = projectService;
    }

    @GetMapping("/all")
    public String findAll(Model model) {
        model.addAttribute("projects", projectService.findAll());

        return "project/projects_all";
    }
}
