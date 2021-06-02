package com.lewandowski.wycena3000.service;

import com.lewandowski.wycena3000.dto.PartChangeRequestDto;
import com.lewandowski.wycena3000.entity.*;
import com.lewandowski.wycena3000.repository.PartRepository;
import com.lewandowski.wycena3000.repository.PartTypeRepository;
import com.lewandowski.wycena3000.repository.ProjectRepository;
import org.springframework.stereotype.Service;

import javax.persistence.EntityNotFoundException;
import javax.transaction.Transactional;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
@Transactional
public class PartService {

    private final PartTypeRepository partTypeRepository;
    private final PartRepository partRepository;
    private final ProjectRepository projectRepository;

    public PartService(PartTypeRepository partTypeRepository, PartRepository partRepository, ProjectRepository projectRepository) {
        this.partTypeRepository = partTypeRepository;
        this.partRepository = partRepository;
        this.projectRepository = projectRepository;
    }

    public List<Part> findAll() {
        return partRepository.findAll();
    }

    public Part save(Part part) {
        return this.partRepository.save(part);
    }

    public List<PartType> getPartTypes() {
        return partTypeRepository.findAll();
    }


    public List<Part> getParts() {
        return partRepository.findAll();
    }

    public List<Part> getPartsByUser(User user) {
        return partRepository.findAllByUserIdOrderByIdAsc(user.getId());
    }

    public Part findById(long partId) {
        return partRepository
                .findById(partId)
                .orElseThrow(() -> new EntityNotFoundException("part with given Id doesn't exist"));
    }

    public void delete(long partId) {
        Part part = this.findById(partId);
        this.partRepository.delete(part);
    }

    public List<PartType> getPartTypesByProject(Long projectId) {
        return partTypeRepository.findByProjectId(projectId);
    }

    public Set<Long> getEnabledDeleteSet() {
        return partRepository.enabledDeleteSet();
    }

    public void changePartInProject(PartChangeRequestDto changeDto) {

        Project project = projectRepository.findById(changeDto.getProjectId())
                .orElseThrow(() -> new EntityNotFoundException("Project with id '" + changeDto.getProjectId() + "' doesn't exist"));

        Map<Part, Integer> parts = project.getParts();
        Part newPart = this.findById(changeDto.getNewPartId());
        Part oldPart = this.findById(changeDto.getOldPartId());

        if (parts.containsKey(newPart)) {
            int oldAmount = parts.get(newPart);
            int newAmount = parts.get(oldPart);

            parts.put(newPart, oldAmount + newAmount);
            parts.put(oldPart, 0);
        } else {
            int amount = parts.get(oldPart);

            parts.put(newPart, amount);
            parts.put(oldPart, 0);
        }

        // removing entries with amount of board set to 0
        parts.entrySet().removeIf(entry -> entry.getValue() == 0);

        projectRepository.save(project);
    }
}
