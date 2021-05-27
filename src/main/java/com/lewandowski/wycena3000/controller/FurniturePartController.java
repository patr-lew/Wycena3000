package com.lewandowski.wycena3000.controller;

import com.lewandowski.wycena3000.entity.FurniturePart;
import com.lewandowski.wycena3000.entity.FurniturePartType;
import com.lewandowski.wycena3000.dto.PartChangeRequestDto;
import com.lewandowski.wycena3000.entity.Project;
import com.lewandowski.wycena3000.service.FurniturePartService;
import com.lewandowski.wycena3000.service.ProjectService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@Controller
@RequestMapping("/creator/parts")
public class FurniturePartController {

    private final FurniturePartService furniturePartService;
    private final ProjectService projectService;

    public FurniturePartController(FurniturePartService furniturePartService, ProjectService projectService) {
        this.furniturePartService = furniturePartService;
        this.projectService = projectService;
    }

    @GetMapping("/add")
    public String add(Model model) {
        FurniturePart furniturePart = new FurniturePart();
        model.addAttribute("furniturePart", furniturePart);

        List<FurniturePartType> partTypes = furniturePartService.getFurniturePartTypes();
        model.addAttribute("partTypes", partTypes);

        return "part/part_add";
    }

    @PostMapping("/add")
    public String save(@Valid FurniturePart furniturePart, BindingResult result, Model model) {

        if (result.hasErrors()) {
            model.addAttribute("partTypes", furniturePartService.getFurniturePartTypes());
            return "part/part_add";
        }

        furniturePartService.save(furniturePart);

        return "redirect:/creator/parts/all";
    }

    @GetMapping("/edit")
    public String edit(@RequestParam long partId, Model model) {
        FurniturePart furniturePart = furniturePartService.findById(partId);
        model.addAttribute("furniturePart", furniturePart);

        List<FurniturePartType> partTypes = furniturePartService.getFurniturePartTypes();
        model.addAttribute("partTypes", partTypes);

        return "part/part_edit";
    }

    @GetMapping("/all")
    public String all(Model model) {
        List<FurniturePart> furnitureParts = furniturePartService.getFurnitureParts();
        model.addAttribute("furnitureParts", furnitureParts);

        return "part/part_all";
    }

    @GetMapping("/change")
    public String changePart(@RequestParam Long partId,
                         @RequestParam Long projectId, Model model) {
        Project project = projectService.findById(projectId);
        List<FurniturePart> parts = furniturePartService.findAll();

        model.addAttribute("project", project);
        model.addAttribute("oldPartId", partId);
        model.addAttribute("parts", parts);
        return "part/part_change";
    }

    @PostMapping("/change")
    public String changePart(@ModelAttribute PartChangeRequestDto dto) {
        furniturePartService.changePartInProject(dto);

        return "redirect:/creator/projects/details/" + dto.getProjectId();

    }


}
