package com.lewandowski.wycena3000.service;

import com.lewandowski.wycena3000.dto.PartChangeRequestDto;
import com.lewandowski.wycena3000.entity.*;
import com.lewandowski.wycena3000.repository.FurniturePartRepository;
import com.lewandowski.wycena3000.repository.FurniturePartTypeRepository;
import com.lewandowski.wycena3000.repository.ProjectRepository;
import org.springframework.stereotype.Service;

import javax.persistence.EntityNotFoundException;
import javax.transaction.Transactional;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
@Transactional
public class FurniturePartService {

    private final FurniturePartTypeRepository furniturePartTypeRepository;
    private final FurniturePartRepository furniturePartRepository;
    private final ProjectRepository projectRepository;

    public FurniturePartService(FurniturePartTypeRepository furniturePartTypeRepository, FurniturePartRepository furniturePartRepository, ProjectRepository projectRepository) {
        this.furniturePartTypeRepository = furniturePartTypeRepository;
        this.furniturePartRepository = furniturePartRepository;
        this.projectRepository = projectRepository;
    }

    public List<FurniturePart> findAll() {
        return furniturePartRepository.findAll();
    }

    public FurniturePart save(FurniturePart furniturePart) {
        return furniturePartRepository.save(furniturePart);
    }

    public List<FurniturePartType> getFurniturePartTypes() {
        return furniturePartTypeRepository.findAll();
    }


    public List<FurniturePart> getFurnitureParts() {
        return furniturePartRepository.findAll();
    }

    public FurniturePart findById(long partId) {
        return furniturePartRepository
                .findById(partId)
                .orElseThrow(() -> new EntityNotFoundException("FurniturePart with given Id doesn't exist"));
    }

    public List<FurniturePartType> getFurniturePartTypesByProject(Long projectId) {
        return furniturePartTypeRepository.findByProjectId(projectId);
    }

    public void changePartInProject(PartChangeRequestDto changeDto) {

        Project project = projectRepository.findById(changeDto.getProjectId())
                .orElseThrow(() -> new EntityNotFoundException("Project with id '" + changeDto.getProjectId() + "' doesn't exist"));

        Map<FurniturePart, Integer> furnitureParts = project.getFurnitureParts();
        FurniturePart newPart = this.findById(changeDto.getNewPartId());
        FurniturePart oldPart = this.findById(changeDto.getOldPartId());

        if (furnitureParts.containsKey(newPart)) {
            int oldAmount = furnitureParts.get(newPart);
            int newAmount = furnitureParts.get(oldPart);

            furnitureParts.put(newPart, oldAmount + newAmount);
            furnitureParts.put(oldPart, 0);
        } else {
            int amount = furnitureParts.get(oldPart);

            furnitureParts.put(newPart, amount);
            furnitureParts.put(oldPart, 0);
        }

        // removing entries with amount of board set to 0
        Set<Map.Entry<FurniturePart, Integer>> entrySet = furnitureParts.entrySet();
        Iterator<Map.Entry<FurniturePart, Integer>> iterator = entrySet.iterator();

        while (iterator.hasNext()) {
            Map.Entry<FurniturePart, Integer> entry = iterator.next();
            if (entry.getValue() == 0) {
                iterator.remove();
            }
        }

        projectRepository.save(project);
    }
}
