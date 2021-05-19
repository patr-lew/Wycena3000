package com.lewandowski.wycena3000.controller;

import com.lewandowski.wycena3000.entity.FurniturePart;
import com.lewandowski.wycena3000.entity.FurniturePartType;
import com.lewandowski.wycena3000.service.FurniturePartService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequestMapping("/creator/parts")
public class FurniturePartController {

    private final FurniturePartService furniturePartService;

    public FurniturePartController(FurniturePartService furniturePartService) {
        this.furniturePartService = furniturePartService;
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
    public String save(@ModelAttribute FurniturePart furniturePart) {
        furniturePartService.save(furniturePart);

        return "redirect:/creator/parts/add";
    }



}
