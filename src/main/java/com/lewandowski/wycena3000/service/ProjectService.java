package com.lewandowski.wycena3000.service;

import com.lewandowski.wycena3000.entity.BoardMeasurement;
import com.lewandowski.wycena3000.entity.FurniturePart;
import com.lewandowski.wycena3000.entity.Project;
import com.lewandowski.wycena3000.repository.BoardMeasurementRepository;
import com.lewandowski.wycena3000.repository.FurniturePartRepository;
import com.lewandowski.wycena3000.repository.ProjectRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.persistence.EntityNotFoundException;
import javax.transaction.Transactional;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final FurniturePartRepository furniturePartRepository;
    private final BoardMeasurementRepository boardMeasurementRepository;


    public ProjectService(ProjectRepository projectRepository, FurniturePartRepository furniturePartRepository, BoardMeasurementRepository boardMeasurementRepository) {
        this.projectRepository = projectRepository;
        this.furniturePartRepository = furniturePartRepository;
        this.boardMeasurementRepository = boardMeasurementRepository;
    }

    public List<Project> findAll() {
        return projectRepository.findAllByOrderByCreatedAsc();
    }

    public Project save(Project project) {
        return projectRepository.save(project);
    }

    public Optional<Project> findById(long id) {
        return projectRepository.findById(id);
    }

    /**
     * updates the number of furnitureParts objects in relation to the project
     * by updating the value in furnitureParts hashMap
     */
    public Project addFurniturePartsToProject(Project project, long furniturePartId, int newAmount) {

        FurniturePart addedPart = furniturePartRepository
                .findById(furniturePartId)
                .orElseThrow(() -> new EntityNotFoundException());
        Map<FurniturePart, Integer> furnitureParts = project.getFurnitureParts();

        if (furnitureParts.containsKey(addedPart)) {
            int existingAmount = furnitureParts.get(addedPart);
            newAmount += existingAmount;
        }

        furnitureParts.put(addedPart, newAmount);

        return projectRepository.save(project);
    }

    public Project addBoardMeasurementToProject(Project project, BoardMeasurement addedBoardMeasurement) {

        List<BoardMeasurement> boardMeasurementsInDb = boardMeasurementRepository.findAll();

        if(!boardMeasurementsInDb.contains(addedBoardMeasurement)) {
            boardMeasurementRepository.save(addedBoardMeasurement);
        }

        Map<BoardMeasurement, Integer> boardMeasurementsInProject = project.getBoardMeasurements();
        int newAmount = addedBoardMeasurement.getAmount();
        log.info(addedBoardMeasurement.getAmount() + "\n\n\n");

        if(boardMeasurementsInProject.containsKey(addedBoardMeasurement)) {
            int existingAmount = boardMeasurementsInProject.get(addedBoardMeasurement);
            newAmount += existingAmount;
        }

        boardMeasurementsInProject.put(addedBoardMeasurement, newAmount);

        return projectRepository.save(project);
    }

    public List<String> computeMarginList(List<Project> projects) {
        return projects.stream()
                .map(this::computeMargin)
                .collect(Collectors.toList());

    }

    public String computeMargin(Project project) {

        if (null == project.getTotalCost() || null == project.getPrice() ||
                BigDecimal.ZERO == project.getTotalCost() || BigDecimal.ZERO == project.getPrice()) {
            return "-";
        }

        String margin = project.getPrice()
                .divide(project.getTotalCost(), 2, RoundingMode.HALF_UP)
                .subtract(BigDecimal.ONE)
                .multiply(BigDecimal.valueOf(100))
                .setScale(0, RoundingMode.HALF_UP)
                .toString();

        return margin + "%";
    }
}

