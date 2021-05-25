package com.lewandowski.wycena3000.service;

import com.lewandowski.wycena3000.dto.AddingPartDto;
import com.lewandowski.wycena3000.dto.BoardByProjectDto;
import com.lewandowski.wycena3000.dto.NewPriceRequestDto;
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
    private final BigDecimal MILLIMETER_TO_METER_CONVERSION = BigDecimal.valueOf(1000);


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
        project.setMargin(calculateMargin(project));
        return projectRepository.save(project);
    }

    public Project findById(long id) {
        return projectRepository
                .findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Project with id '" + id + "' doesn't exist"));
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
    public Project addFurniturePartsToProject(AddingPartDto partDto) {
        Project project = findById(partDto.getProjectId());
        int newAmount = partDto.getAmount();

        FurniturePart addedPart = furniturePartRepository
                .findById(partDto.getFurniturePartId())
                .orElseThrow(() -> new EntityNotFoundException("FurniturePart with ID '" + partDto.getFurniturePartId() + "' not found."));
        Map<FurniturePart, Integer> furnitureParts = project.getFurnitureParts();

        if (furnitureParts.containsKey(addedPart)) {
            int existingAmount = furnitureParts.get(addedPart);
            newAmount += existingAmount;
        }

        furnitureParts.put(addedPart, newAmount);

        return save(project);
    }

    public Project addBoardMeasurementToProject(Long projectId, BoardMeasurement addedBoardMeasurement) {
        Project project = findById(projectId);

        List<BoardMeasurement> boardMeasurementsInDb = boardMeasurementRepository.findAll();

        if (!boardMeasurementsInDb.contains(addedBoardMeasurement)) {
            boardMeasurementRepository.save(addedBoardMeasurement);
        }

        Map<BoardMeasurement, Integer> boardMeasurementsInProject = project.getBoardMeasurements();
        int newAmount = addedBoardMeasurement.getAmount();
        BoardMeasurement toBeRemoved = null;

        for (BoardMeasurement measurement : boardMeasurementsInProject.keySet()) {
            if (measurement.getBoard().equals(addedBoardMeasurement.getBoard()) &&
                    measurement.getHeight() == addedBoardMeasurement.getAmount() &&
                    measurement.getWidth() == addedBoardMeasurement.getWidth()) {

                int existingAmount = boardMeasurementsInProject.get(measurement);
                toBeRemoved = measurement;
                newAmount += existingAmount;
            }
        }
        if (null != toBeRemoved) {
            boardMeasurementsInProject.remove(toBeRemoved);
            boardMeasurementRepository.delete(toBeRemoved);
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

        projectById.setProjectDetails(projectDetails);

        projectById.setTotalCost(calculateTotalCost(projectById));
        projectRepository.save(projectById);
    }

    public List<String> computeMarginList(List<Project> projects) {
        return projects.stream()
                .map(this::marginToString)
                .collect(Collectors.toList());

    }

    public String marginToString(Project project) {

        if (null == project.getTotalCost() || null == project.getPrice() || null == project.getMargin() ||
                BigDecimal.ZERO.compareTo(project.getTotalCost()) == 0 || BigDecimal.ZERO.compareTo(project.getPrice()) == 0) {
            return "-";
        }

        String margin = project.getMargin()
                .setScale(0, RoundingMode.HALF_UP)
                .toString();
        return margin + "%";
    }

    public void saveProjectDetails(ProjectDetails projectDetails) {
        projectDetailsRepository.save(projectDetails);
    }

    public BigDecimal calculateTotalCost(Project project) {
        BigDecimal totalCost = BigDecimal.ZERO;


        // add costs from ProjectDetails
        if (null != project.getProjectDetails()) {
            ProjectDetails projectDetails = project.getProjectDetails();

            totalCost = totalCost.add(projectDetails.getMontageCost())
                    .add(projectDetails.getWorkerCost())
                    .add(projectDetails.getOtherCosts());

        }

        // add costs from boards
        Hibernate.initialize(project.getBoardMeasurements());
        if (null != project.getBoardMeasurements()) {
            Map<BoardMeasurement, Integer> boardMeasurements = project.getBoardMeasurements();
            Map<Board, BigDecimal> boardArea = new HashMap<>();


            for (BoardMeasurement boardMeasurement : boardMeasurements.keySet()) {
                BigDecimal boardSurfaceArea = getBoardSurfaceArea(boardMeasurements, boardMeasurement);

                if (boardArea.containsKey(boardMeasurement.getBoard())) {
                    boardSurfaceArea = boardSurfaceArea.add(boardArea.get(boardMeasurement.getBoard()));
                }

                boardArea.put(boardMeasurement.getBoard(), boardSurfaceArea);
            }


            for (Board board : boardArea.keySet()) {
                BigDecimal amountOfBoards = boardArea.get(board);
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

    public List<BoardByProjectDto> getBoardsDetailsByProject(Long projectId) {
        Map<Long, BoardByProjectDto> boardsDetails = new HashMap<>();
        Project project = findById(projectId);
        Hibernate.initialize(project.getBoardMeasurements());

        if (null == project.getBoardMeasurements()) {
            BoardByProjectDto boardDto = new BoardByProjectDto();
            boardDto.setBoardId(0L);
            boardDto.setName("Brak płyt");
            return List.of(boardDto);
        }

        Map<BoardMeasurement, Integer> boardMeasurements = project.getBoardMeasurements();

        // map boardMeasurements to boardsDetails (Entity to Dto)
        for (BoardMeasurement measurement : boardMeasurements.keySet()) {
            Board board = measurement.getBoard();
            BoardByProjectDto boardDto = new BoardByProjectDto();

            long givenBoardId = board.getId();
            if (boardsDetails.containsKey(givenBoardId)) {
                boardDto = boardsDetails.get(givenBoardId);
            } else {
                boardDto.setBoardId(givenBoardId);
                boardDto.setName(board.getName());
            }

            BigDecimal totalArea = getBoardSurfaceArea(boardMeasurements, measurement);
            totalArea = totalArea.add(boardDto.getTotalArea());
            boardDto.setTotalArea(totalArea);

            BigDecimal costPerM2 = board.getPricePerM2();
            boardDto.setTotalCost(totalArea.multiply(costPerM2));

            boardsDetails.put(boardDto.getBoardId(), boardDto);
        }

        // scale BigDecimals to format of 0.00
        boardsDetails.values().forEach(dto -> {
            dto.setTotalCost(dto.getTotalCost().setScale(2, RoundingMode.HALF_UP));
            dto.setTotalArea(dto.getTotalArea().setScale(2, RoundingMode.HALF_UP));
        });

        return new ArrayList<>(boardsDetails.values());
    }

    /**
     * Set new price based on data from the form. If both new price and new margin
     * are given, calculate based on new price. Otherwise calculate based on one given
     * parameter
     */
    public void setNewPrice(NewPriceRequestDto newPriceRequestDto) {
        Project project = findById(newPriceRequestDto.getProjectId());

        if (null != newPriceRequestDto.getPrice()) {
            BigDecimal price = newPriceRequestDto.getPrice();
            project.setPrice(price);
            project.setMargin(calculateMargin(project));
        } else if (null != newPriceRequestDto.getMargin()) {
            BigDecimal margin = BigDecimal.valueOf(newPriceRequestDto.getMargin());
            project.setMargin(margin);
            updatePrice(project, margin);
        }

        projectRepository.save(project);
    }

    private void updatePrice(Project project, BigDecimal margin) {
        BigDecimal cost = project.getTotalCost();
        BigDecimal newPrice = cost.multiply(margin).divide(BigDecimal.valueOf(100), RoundingMode.HALF_UP);
        newPrice = newPrice.add(cost);
        project.setPrice(newPrice);
    }

    private BigDecimal calculateMargin(Project project) {
        return project.getPrice()
                .divide(project.getTotalCost(), 2, RoundingMode.HALF_UP)
                .subtract(BigDecimal.ONE)
                .multiply(BigDecimal.valueOf(100))
                .setScale(2, RoundingMode.HALF_UP);

    }

    private BigDecimal getBoardSurfaceArea(Map<BoardMeasurement, Integer> boardMeasurements, BoardMeasurement boardMeasurement) {
        BigDecimal width = BigDecimal.valueOf(boardMeasurement.getWidth())
                .divide(MILLIMETER_TO_METER_CONVERSION, 4, RoundingMode.HALF_UP);
        BigDecimal height = BigDecimal.valueOf(boardMeasurement.getHeight())
                .divide(MILLIMETER_TO_METER_CONVERSION, 4, RoundingMode.HALF_UP);

        BigDecimal boardSurfaceArea =
                width.multiply(height).multiply(BigDecimal.valueOf(boardMeasurements.get(boardMeasurement)));
        return boardSurfaceArea;
    }

}

