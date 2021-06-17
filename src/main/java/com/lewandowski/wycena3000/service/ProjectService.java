package com.lewandowski.wycena3000.service;

import com.lewandowski.wycena3000.dto.AddPartToProjectRequestDto;
import com.lewandowski.wycena3000.dto.BoardsByProjectResponseDto;
import com.lewandowski.wycena3000.dto.NewPriceRequestDto;
import com.lewandowski.wycena3000.entity.*;
import com.lewandowski.wycena3000.exception.NegativeAmountException;
import com.lewandowski.wycena3000.repository.MeasurementRepository;
import com.lewandowski.wycena3000.repository.PartRepository;
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
    private final PartRepository partRepository;
    private final MeasurementRepository measurementRepository;
    private final BigDecimal MILLIMETER_TO_METER_CONVERSION = BigDecimal.valueOf(1000);


    public ProjectService(ProjectRepository projectRepository, ProjectDetailsRepository projectDetailsRepository, PartRepository partRepository, MeasurementRepository measurementRepository) {
        this.projectRepository = projectRepository;
        this.projectDetailsRepository = projectDetailsRepository;
        this.partRepository = partRepository;
        this.measurementRepository = measurementRepository;
    }

    public List<Project> findAll() {
        return projectRepository.findAllByOrderByCreatedAsc();
    }

    public List<Project> findAllByUserId(User user) {
        return projectRepository.findAllByUserIdOrderByCreatedAsc(user.getId());
    }

    public Project saveAndUpdate(Project project) {
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
        Project project = this.findById(id);
        if (!Hibernate.isInitialized(project.getProjectDetails())) {
            Hibernate.initialize(project.getProjectDetails());
        }

        if (!Hibernate.isInitialized(project.getMeasurements())) {
            Hibernate.initialize(project.getMeasurements());
        }

        if (!Hibernate.isInitialized(project.getParts())) {
            Hibernate.initialize(project.getParts());
        }

        return project;
    }

    public void delete(Long projectId) {
        Project projectToDelete = this.findById(projectId);

        deleteProjectRelations(projectToDelete);
        projectRepository.delete(projectToDelete);

        deleteOrphanedMeasurements();
    }

    public Project addPartToProject(AddPartToProjectRequestDto partDto) {
        Project project = findById(partDto.getProjectId());
        int newAmount = partDto.getAmount();

        Part addedPart = partRepository
                .findById(partDto.getPartId())
                .orElseThrow(() -> new EntityNotFoundException("part with ID '" + partDto.getPartId() + "' not found."));

        return project.addPart(addedPart,newAmount);
    }

    public Project addMeasurementToProject(Long projectId, Measurement addedMeasurement) {
        Project project = findById(projectId);

        Project updatedProject = project.addMeasurement(addedMeasurement);

        if (updatedProject.getMeasurements().containsKey(addedMeasurement)) {
            saveMeasurement(addedMeasurement);
        }

        return updatedProject;
    }

    private void saveMeasurement(Measurement addedMeasurement) {
        List<Measurement> measurementsInDb = measurementRepository.findAll();

        if (!measurementsInDb.contains(addedMeasurement)) {
            measurementRepository.save(addedMeasurement);
        }
    }


    public void updateProjectDetailsInProject(long projectId, ProjectDetails newDetails) {
        Project projectById = findById(projectId);

        ProjectDetails details = projectById.getProjectDetails();
        details.setWorkerCost(newDetails.getWorkerCost());
        details.setMontageCost(newDetails.getMontageCost());
        details.setOtherCosts(newDetails.getOtherCosts());

        this.saveAndUpdate(projectById);
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

    public List<BoardsByProjectResponseDto> getBoardsDetailsByProject(Long projectId) {
        Map<Long, BoardsByProjectResponseDto> boardsDetails = new HashMap<>();
        Project project = findById(projectId);
        Hibernate.initialize(project.getMeasurements());

        if (null == project.getMeasurements()) {
            BoardsByProjectResponseDto boardDto = new BoardsByProjectResponseDto();
            boardDto.setBoardId(0L);
            boardDto.setName("Brak płyt");
            return List.of(boardDto);
        }

        Map<Measurement, Integer> measurements = project.getMeasurements();

        // map measurements to boardsDetails (Entity to Dto)
        for (Map.Entry<Measurement, Integer> measurementEntry : measurements.entrySet()) {
            Measurement measurement = measurementEntry.getKey();
            Board board = measurement.getBoard();
            BoardsByProjectResponseDto boardDto = new BoardsByProjectResponseDto();

            long givenBoardId = board.getId();
            if (boardsDetails.containsKey(givenBoardId)) {
                boardDto = boardsDetails.get(givenBoardId);
            } else {
                boardDto.setBoardId(givenBoardId);
                boardDto.setName(board.getName());
            }

            BigDecimal totalArea = getBoardSurfaceArea(measurementEntry);
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


    public void setNewPrice(NewPriceRequestDto newPriceRequestDto) {
        Project project = findById(newPriceRequestDto.getProjectId());

        // If both new price and new margin are given, ignores margin and calculates based on new price
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

    private void deleteProjectRelations(Project projectToDelete) {
        projectToDelete.getMeasurements().clear();
        projectToDelete.getParts().clear();
        projectRepository.save(projectToDelete);
        projectDetailsRepository.delete(projectToDelete.getProjectDetails());
    }

    private void deleteOrphanedMeasurements() {
        Iterator<Measurement> orphansIterator = measurementRepository.findAllOrphans().iterator();
        while (orphansIterator.hasNext()) {
            measurementRepository.delete(orphansIterator.next());
        }
    }

    private BigDecimal calculateTotalCost(Project project) {
        BigDecimal totalCost = BigDecimal.ZERO;


        // add costs from ProjectDetails
        if (null != project.getProjectDetails()) {
            ProjectDetails projectDetails = project.getProjectDetails();

            totalCost = totalCost.add(projectDetails.getMontageCost())
                    .add(projectDetails.getWorkerCost())
                    .add(projectDetails.getOtherCosts());

        }

        // add costs from boards
        Hibernate.initialize(project.getMeasurements());
        if (null != project.getMeasurements()) {
            Map<Measurement, Integer> measurements = project.getMeasurements();
            Map<Board, BigDecimal> boardAreas = new HashMap<>();


            for (Map.Entry<Measurement, Integer> measurementEntry : measurements.entrySet()) {
                BigDecimal boardSurfaceArea = getBoardSurfaceArea(measurementEntry);

                Measurement measurement = measurementEntry.getKey();

                if (boardAreas.containsKey(measurement.getBoard())) {
                    boardSurfaceArea = boardSurfaceArea.add(boardAreas.get(measurement.getBoard()));
                }

                boardAreas.put(measurement.getBoard(), boardSurfaceArea);
            }


            for (Board board : boardAreas.keySet()) {
                BigDecimal amountOfBoards = boardAreas.get(board);
                BigDecimal boardCost = board.getPricePerM2().multiply(amountOfBoards);
                totalCost = totalCost.add(boardCost);
            }
        }

        // add costs from parts
        Hibernate.initialize(project.getParts());
        if (null != project.getParts()) {
            Map<Part, Integer> parts = project.getParts();

            for (Part part : parts.keySet()) {
                BigDecimal amountOfParts = BigDecimal.valueOf(parts.get(part));
                BigDecimal partCost = part.getPrice().multiply(amountOfParts);
                totalCost = totalCost.add(partCost);
            }
        }

        return totalCost;
    }

    private BigDecimal calculateMargin(Project project) {
        if (project.getPrice() == null || project.getTotalCost() == null
                || project.getPrice().compareTo(BigDecimal.ZERO) == 0
                || project.getTotalCost().compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }

        return project.getPrice()
                .divide(project.getTotalCost(), 2, RoundingMode.HALF_UP)
                .subtract(BigDecimal.ONE)
                .multiply(BigDecimal.valueOf(100))
                .setScale(2, RoundingMode.HALF_UP);

    }

    private void updatePrice(Project project, BigDecimal margin) {
        BigDecimal cost = project.getTotalCost();
        BigDecimal newPrice = cost.multiply(margin).divide(BigDecimal.valueOf(100), RoundingMode.HALF_UP);
        newPrice = newPrice.add(cost);
        project.setPrice(newPrice);
    }

    private BigDecimal getBoardSurfaceArea(Map.Entry<Measurement, Integer> measurementEntry) {
        Measurement measurement = measurementEntry.getKey();
        BigDecimal width = BigDecimal.valueOf(measurement.getWidth())
                .divide(MILLIMETER_TO_METER_CONVERSION, 4, RoundingMode.HALF_UP);
        BigDecimal height = BigDecimal.valueOf(measurement.getHeight())
                .divide(MILLIMETER_TO_METER_CONVERSION, 4, RoundingMode.HALF_UP);
        BigDecimal amount = BigDecimal.valueOf(measurementEntry.getValue());

        return width.multiply(height).multiply(amount);
    }
}

