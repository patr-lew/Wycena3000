package com.lewandowski.wycena3000.controller;

import com.lewandowski.wycena3000.entity.Part;
import com.lewandowski.wycena3000.entity.PartType;
import com.lewandowski.wycena3000.dto.PartChangeRequestDto;
import com.lewandowski.wycena3000.entity.Project;
import com.lewandowski.wycena3000.security.CurrentUser;
import com.lewandowski.wycena3000.service.PartService;
import com.lewandowski.wycena3000.service.ProjectService;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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

    @GetMapping("/all")
    public String all(Model model, @AuthenticationPrincipal CurrentUser currentUser) {
        List<Part> parts = partService.getPartsByUser(currentUser.getUser());
        Set<Long> enabledDeleteSet = partService.getEnabledDeleteSet();
        model.addAttribute("parts", parts);
        model.addAttribute("enabledDelete", enabledDeleteSet);

        return "part/part_all";
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
    public String save(@Valid Part part, BindingResult result, Model model, @AuthenticationPrincipal CurrentUser currentUser) {

        if (result.hasErrors()) {
            model.addAttribute("partTypes", partService.getPartTypes());
            return "part/part_add";
        }

        part.setUser(currentUser.getUser());
        partService.save(part);

        return "redirect:/creator/parts/all";
    }

    @GetMapping("/edit/{partId}")
    public String edit(@PathVariable long partId, Model model, @AuthenticationPrincipal CurrentUser currentUser) {
        Part part = partService.findById(partId);
        if (part.getUser().getId() != currentUser.getUser().getId()) {
            throw new AccessDeniedException(String.format("User tried to change part that doesn't belong to him. UserId = %d, boardId = %d",
                    currentUser.getUser().getId(), partId));        }

        model.addAttribute("part", part);

        List<PartType> partTypes = partService.getPartTypes();
        model.addAttribute("partTypes", partTypes);

        return "part/part_edit";
    }

    @GetMapping("/delete/{partId}")
    public String delete(@PathVariable long partId, @AuthenticationPrincipal CurrentUser currentUser) {
        Part part = partService.findById(partId);
        if (part.getUser().getId() != currentUser.getUser().getId()) {
            throw new AccessDeniedException(String.format("User tried to delete part that doesn't belong to him. UserId = %d, boardId = %d",
                    currentUser.getUser().getId(), partId));          }
        partService.delete(partId);

        return "redirect:/creator/parts/all";
    }

    @GetMapping("/change")
    public String changePart(@RequestParam Long partId,
                             @RequestParam Long projectId, Model model,
                             @AuthenticationPrincipal CurrentUser currentUser) {
        Project project = projectService.findById(projectId);
        if(project.getUser().getId() != currentUser.getUser().getId()) {
            throw new AccessDeniedException(String.format("User tried to access change part view that doesn't belong to him. UserId = %d, boardId = %d",
                    currentUser.getUser().getId(), partId));          }
        List<Part> parts = partService.getPartsByUser(currentUser.getUser());

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
