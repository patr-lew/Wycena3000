package com.lewandowski.wycena3000.service;

import com.lewandowski.wycena3000.entity.*;
import com.lewandowski.wycena3000.repository.BoardMeasurementRepository;
import com.lewandowski.wycena3000.repository.FurniturePartRepository;
import com.lewandowski.wycena3000.repository.ProjectDetailsRepository;
import com.lewandowski.wycena3000.repository.ProjectRepository;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.Hibernate;
import org.springframework.stereotype.Service;

import javax.persistence.EntityNotFoundException;
import javax.transaction.Transactional;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final ProjectDetailsRepository projectDetailsRepository;
    private final FurniturePartRepository furniturePartRepository;
    private final BoardMeasurementRepository boardMeasurementRepository;


    public ProjectService(ProjectRepository projectRepository, ProjectDetailsRepository projectDetailsRepository, FurniturePartRepository furniturePartRepository, BoardMeasurementRepository boardMeasurementRepository) {
        this.projectRepository = projectRepository;
        this.projectDetailsRepository = projectDetailsRepository;
        this.furniturePartRepository = furniturePartRepository;
        this.boardMeasurementRepository = boardMeasurementRepository;
    }

    public List<Project> findAll() {
        return projectRepository.findAllByOrderByCreatedAsc();
    }

    public Project save(Project project) {
        project.setTotalCost(calculateTotalCost(project));
        return projectRepository.save(project);
    }

    public Project findById(long id) {
        return projectRepository
                .findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Project with given Id doesn't exist"));
    }

    public Project findByIdEager(long id) {
        Project project = findById(id);
        if (!Hibernate.isInitialized(project.getProjectDetails())) {
            Hibernate.initialize(project.getProjectDetails());
        }

        if (!Hibernate.isInitialized(project.getBoardMeasurements())) {
            Hibernate.initialize(project.getBoardMeasurements());
        }

        if (!Hibernate.isInitialized(project.getFurnitureParts())) {
            Hibernate.initialize(project.getFurnitureParts());
        }

        return project;
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

        return save(project);
    }

    public Project addBoardMeasurementToProject(Project project, BoardMeasurement addedBoardMeasurement) {

        List<BoardMeasurement> boardMeasurementsInDb = boardMeasurementRepository.findAll();

        if (!boardMeasurementsInDb.contains(addedBoardMeasurement)) {
            boardMeasurementRepository.save(addedBoardMeasurement);
        }

        Map<BoardMeasurement, Integer> boardMeasurementsInProject = project.getBoardMeasurements();
        int newAmount = addedBoardMeasurement.getAmount();
        log.info(addedBoardMeasurement.getAmount() + "\n\n\n");

        if (boardMeasurementsInProject.containsKey(addedBoardMeasurement)) {
            int existingAmount = boardMeasurementsInProject.get(addedBoardMeasurement);
            newAmount += existingAmount;
        }

        boardMeasurementsInProject.put(addedBoardMeasurement, newAmount);

        return save(project);
    }

    public void addProjectDetailsToProject(long projectId, ProjectDetails projectDetails) {
        Project projectById = findById(projectId);

        // TODO update a ProjectDetails without actually deleting it first
        if (null != projectById.getProjectDetails()) {
            ProjectDetails projectDetailsToRemove = projectById.getProjectDetails();
            projectDetailsRepository.delete(projectDetailsToRemove);
        }

        projectDetails.setProject(projectById);
        projectDetailsRepository.save(projectDetails);

        projectById.setTotalCost(calculateTotalCost(projectById));
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

    public void saveProjectDetails(ProjectDetails projectDetails) {
        projectDetailsRepository.save(projectDetails);
    }

    public BigDecimal calculateTotalCost(Project project) {
        BigDecimal totalCost =project.getTotalCost();

        // add costs from ProjectDetails
        if(null != project.getProjectDetails()) {
            ProjectDetails projectDetails = project.getProjectDetails();

            totalCost = totalCost.add(projectDetails.getMontageCost())
                    .add(projectDetails.getWorkerCost())
                    .add(projectDetails.getOtherCosts());

        }

        // add costs from boards
        Hibernate.initialize(project.getBoardMeasurements());
        if (null != project.getBoardMeasurements()) {
            Map<BoardMeasurement, Integer> boardMeasurements = project.getBoardMeasurements();
            Map<Board, Integer> boardArea = new HashMap<>();


            for (BoardMeasurement boardMeasurement : boardMeasurements.keySet()) {
                int boardSurfaceArea = boardMeasurement.getHeight() * boardMeasurement.getWidth() * boardMeasurement.getAmount();
                if (boardArea.containsKey(boardMeasurement.getBoard())) {
                    boardSurfaceArea += boardArea.get(boardMeasurement.getBoard());
                }

                boardArea.put(boardMeasurement.getBoard(), boardSurfaceArea);
            }


            for (Board board : boardArea.keySet()) {
                BigDecimal amountOfBoards = BigDecimal.valueOf(boardArea.get(board));
                BigDecimal boardCost = board.getPricePerM2().multiply(amountOfBoards);
                totalCost = totalCost.add(boardCost);
            }
        }


        // add costs from parts
        Hibernate.initialize(project.getFurnitureParts());
        if (null != project.getFurnitureParts()) {
            Map<FurniturePart, Integer> furnitureParts = project.getFurnitureParts();

           for (FurniturePart part : furnitureParts.keySet()) {
                BigDecimal amountOfParts = BigDecimal.valueOf(furnitureParts.get(part));
                BigDecimal partCost = part.getPrice().multiply(amountOfParts);
                totalCost = totalCost.add(partCost);
            }
        }

        return totalCost;
    }
}

