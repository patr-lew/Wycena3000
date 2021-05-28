package com.lewandowski.wycena3000.controller;

import com.lewandowski.wycena3000.entity.Part;
import com.lewandowski.wycena3000.entity.PartType;
import com.lewandowski.wycena3000.dto.PartChangeRequestDto;
import com.lewandowski.wycena3000.entity.Project;
import com.lewandowski.wycena3000.service.PartService;
import com.lewandowski.wycena3000.service.ProjectService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.Set;

@Controller
@RequestMapping("/creator/parts")
public class PartController {

    private final PartService partService;
    private final ProjectService projectService;

    public PartController(PartService partService, ProjectService projectService) {
        this.partService = partService;
        this.projectService = projectService;
    }

    @GetMapping("/add")
    public String add(Model model) {
        Part part = new Part();
        model.addAttribute("part", part);

        List<PartType> partTypes = partService.getPartTypes();
        model.addAttribute("partTypes", partTypes);

        return "part/part_add";
    }

    @PostMapping("/add")
    public String save(@Valid Part part, BindingResult result, Model model) {

        if (result.hasErrors()) {
            model.addAttribute("partTypes", partService.getPartTypes());
            return "part/part_add";
        }

        partService.save(part);

        return "redirect:/creator/parts/all";
    }

    @GetMapping("/edit")
    public String edit(@RequestParam long partId, Model model) {
        Part part = partService.findById(partId);
        model.addAttribute("part", part);

        List<PartType> partTypes = partService.getPartTypes();
        model.addAttribute("partTypes", partTypes);

        return "part/part_edit";
    }

    @GetMapping("/delete/{partId}")
    public String delete(@PathVariable long partId) {
        partService.delete(partId);

        return "redirect:/creator/parts/all";
    }

    @GetMapping("/all")
    public String all(Model model) {
        List<Part> parts = partService.getParts();
        Set<Long> enabledDeleteSet = partService.getEnabledDeleteSet();
        model.addAttribute("parts", parts);
        model.addAttribute("enabledDelete", enabledDeleteSet);

        return "part/part_all";
    }

    @GetMapping("/change")
    public String changePart(@RequestParam Long partId,
                         @RequestParam Long projectId, Model model) {
        Project project = projectService.findById(projectId);
        List<Part> parts = partService.findAll();

        model.addAttribute("project", project);
        model.addAttribute("oldPartId", partId);
        model.addAttribute("parts", parts);
        return "part/part_change";
    }

    @PostMapping("/change")
    public String changePart(@ModelAttribute PartChangeRequestDto dto) {
        partService.changePartInProject(dto);

        return "redirect:/creator/projects/details/" + dto.getProjectId();

    }


}
