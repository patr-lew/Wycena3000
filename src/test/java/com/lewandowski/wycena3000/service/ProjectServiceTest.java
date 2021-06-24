package com.lewandowski.wycena3000.service;

import com.lewandowski.wycena3000.dto.AddPartToProjectRequestDto;
import com.lewandowski.wycena3000.dto.BoardsByProjectResponseDto;
import com.lewandowski.wycena3000.entity.*;
import com.lewandowski.wycena3000.exception.NegativeAmountException;
import com.lewandowski.wycena3000.repository.MeasurementRepository;
import com.lewandowski.wycena3000.repository.PartRepository;
import com.lewandowski.wycena3000.repository.ProjectDetailsRepository;
import com.lewandowski.wycena3000.repository.ProjectRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProjectServiceTest {
    @Mock
    private ProjectRepository projectRepository;
    @Mock
    private ProjectDetailsRepository projectDetailsRepository;
    @Mock
    private PartRepository partRepository;
    @Mock
    private MeasurementRepository measurementRepository;

    @InjectMocks
    private ProjectService projectService;


    // save() -> calculateTotalCost()
    @Test
    public void givenNoCosts_whenSavingProject_updateTotalCostToZero() {
        // given
        final BigDecimal zero_cost = BigDecimal.ZERO;
        Project testProject = new Project();
        when(projectRepository.save(any())).thenReturn(testProject);

        // when
        Project savedProject = projectService.saveAndUpdate(testProject);

        // then
        assertThat(savedProject.getTotalCost()).isEqualTo(zero_cost);
    }

    @Test
    public void givenProjectDetailCosts_whenSavingProject_updateTotalCost() {
        // given
        final BigDecimal workersCost = BigDecimal.valueOf(1);
        final BigDecimal montageCost = BigDecimal.valueOf(2);
        final BigDecimal otherCost = BigDecimal.valueOf(4);
        final BigDecimal totalCost = BigDecimal.valueOf(7);

        ProjectDetails projectDetails = new ProjectDetails();
        Project testProject = new Project();
        testProject.setProjectDetails(projectDetails);

        projectDetails.setWorkerCost(workersCost);
        projectDetails.setMontageCost(montageCost);
        projectDetails.setOtherCosts(otherCost);

        when(projectRepository.save(any())).thenReturn(testProject);

        // when
        Project savedProject = projectService.saveAndUpdate(testProject);

        // then
        assertThat(savedProject.getTotalCost()).isEqualTo(totalCost);
    }

    @Test
    public void givenMeasurementsCosts_whenSavingProject_updateTotalCost() {
        // given
        final Integer firstAmount = 10;
        final Integer secondAmount = 100;
        final BigDecimal pricePerM2 = BigDecimal.TEN;
        final BigDecimal totalCost = BigDecimal.valueOf(1.1);

        Board board = new Board();
        board.setPricePerM2(pricePerM2);

        Measurement measurement1 = new Measurement();
        measurement1.setBoard(board);
        measurement1.setHeight(100);
        measurement1.setWidth(100);

        Measurement measurement2 = new Measurement();
        measurement2.setBoard(board);
        measurement2.setHeight(10);
        measurement2.setWidth(10);

        Map<Measurement, Integer> testMeasurements = new HashMap<>();
        testMeasurements.put(measurement1, firstAmount);
        testMeasurements.put(measurement2, secondAmount);

        Project testProject = new Project();
        testProject.setMeasurements(testMeasurements);

        when(projectRepository.save(any())).thenReturn(testProject);

        // when
        Project savedProject = projectService.saveAndUpdate(testProject);

        // then
        assertThat(savedProject.getTotalCost()).isEqualByComparingTo(totalCost);

    }

    @Test
    public void givenPartCosts_whenSavingProject_updateTotalCost() {
        // given
        final Integer firstAmount = 10;
        final Integer secondAmount = 100;
        final BigDecimal firstPrice = BigDecimal.valueOf(100);
        final BigDecimal secondPrice = BigDecimal.valueOf(10);
        final BigDecimal totalCost = BigDecimal.valueOf(2000);

        Part part1 = new Part();
        part1.setPrice(firstPrice);
        Part part2 = new Part();
        part2.setPrice(secondPrice);

        Map<Part, Integer> testParts = new HashMap<>();
        testParts.put(part1, firstAmount);
        testParts.put(part2, secondAmount);

        Project testProject = new Project();
        testProject.setParts(testParts);

        when(projectRepository.save(any())).thenReturn(testProject);

        // when
        Project savedProject = projectService.saveAndUpdate(testProject);

        // then
        assertThat(savedProject.getTotalCost()).isEqualByComparingTo(totalCost);
    }

    // save() -> calculateMargin()
    @Test
    public void givenUpdatedPrice_whenSavingProject_updateCalculatedProjectMargin() {
        // given
        final BigDecimal totalCost = BigDecimal.valueOf(100);
        final BigDecimal projectPrice = BigDecimal.valueOf(200);
        final BigDecimal margin = BigDecimal.valueOf(100);

        ProjectDetails sourceOfCosts = new ProjectDetails();
        sourceOfCosts.setOtherCosts(totalCost);
        Project testProject = new Project();
        testProject.setProjectDetails(sourceOfCosts);
        testProject.setPrice(projectPrice);

        when(projectRepository.save(any())).thenReturn(testProject);

        // when
        Project savedProject = projectService.saveAndUpdate(testProject);

        // then
        assertThat(savedProject.getMargin()).isEqualByComparingTo(margin);
    }

    @Test
    public void givenEmptyProject_whenSavingProject_setMarginToZero() {
        // given
        final BigDecimal margin = BigDecimal.ZERO;
        Project testProject = new Project();

        when(projectRepository.save(any())).thenReturn(testProject);

        // when
        Project savedProject = projectService.saveAndUpdate(testProject);

        // then
        assertThat(savedProject.getMargin()).isEqualByComparingTo(margin);
    }

    // addPartToProject
    @Test
    public void givenNewPart_whenAddingToProject_addPart() {
        // given
        final Long projectId = 7L;
        final Long partId = 11L;
        final Integer amount = 23;
        final String partName = "Part name";

        Part newPart = new Part();
        newPart.setId(partId);
        newPart.setName(partName);

        Project testProject = new Project();
        testProject.setId(projectId);
        testProject.setParts(new HashMap<>());

        AddPartToProjectRequestDto dto = new AddPartToProjectRequestDto(projectId, partId, amount);

        when(partRepository.findById(any())).thenReturn(Optional.of(newPart));
        when(projectRepository.findById(any())).thenReturn(Optional.of(testProject));

        // when
        Project savedProject = projectService.addPartToProject(dto);

        // then
        Part savedPart = savedProject.getParts()
                .keySet().stream()
                .findFirst()
                .get();
        assertThat(savedPart.getName()).isEqualTo(partName);
        assertThat(savedProject.getParts().get(savedPart)).isEqualTo(amount);
    }

    @Test
    public void givenExistingPart_whenAddingToProject_updatePartsAmount() {
        // given
        final Long partId = 7L;
        final Long projectId = 11L;
        final Integer oldAmount = 5;
        final Integer newAmount = 20;
        final String partName = "Part name";

        Part existingPart = new Part();
        existingPart.setId(partId);
        existingPart.setName(partName);

        Project testProject = new Project();
        testProject.setId(projectId);
        Map<Part, Integer> parts = new HashMap<>();
        parts.put(existingPart, oldAmount);
        testProject.setParts(parts);

        AddPartToProjectRequestDto dto = new AddPartToProjectRequestDto(projectId, partId, newAmount);

        when(partRepository.findById(partId)).thenReturn(Optional.of(existingPart));
        when(projectRepository.findById(projectId)).thenReturn(Optional.of(testProject));

        // when
        Project savedProject = projectService.addPartToProject(dto);

        // then
        assertThat(savedProject.getParts().size()).isEqualTo(1);
        Part savedPart = savedProject.getParts().keySet().stream().findFirst().get();

        assertThat(savedProject.getParts().get(savedPart)).isEqualTo(oldAmount + newAmount);
    }

    @Test
    public void givenExistingPart_whenSubtractingFromProjectTooMany_throwException() {
        // given
        final Long partId = 7L;
        final Long projectId = 11L;
        final Integer oldAmount = 5;
        final Integer newAmount = -20;
        final String partName = "Part name";

        Part existingPart = Part.builder()
                .id(partId)
                .name(partName)
                .build();

        Map<Part, Integer> parts = new HashMap<>();
        parts.put(existingPart, oldAmount);

        Project testProject = Project.builder()
                .id(projectId)
                .parts(parts)
                .build();

        AddPartToProjectRequestDto dto = new AddPartToProjectRequestDto(projectId, partId, newAmount);

        when(partRepository.findById(partId)).thenReturn(Optional.of(existingPart));
        when(projectRepository.findById(projectId)).thenReturn(Optional.of(testProject));

        // when + then
        assertThatThrownBy(
                () -> projectService.addPartToProject(dto))
                .isInstanceOf(NegativeAmountException.class);

    }

    // addBoardMeasurementToProject
    @Test
    public void givenNewMeasurement_whenAddingToProject_AddMeasurement() {
        // given
        final Long projectId = 17L;
        final Integer amount = 13;

        Measurement addedMeasurement = Measurement.builder()
                .amount(amount)
                .build();

        Project testProject = Project.builder()
                .measurements(new HashMap<>())
                .build();

        when(projectRepository.findById(projectId)).thenReturn(Optional.of(testProject));

        // when
        Project savedProject = projectService.addMeasurementToProject(projectId, addedMeasurement);

        // then
        assertThat(savedProject.getMeasurements().keySet()).containsOnly(addedMeasurement);
        assertThat(savedProject.getMeasurements().get(addedMeasurement)).isEqualTo(amount);
    }

    @Test
    public void givenExistingMeasurement_whenAddingToProject_updateMeasurementsAmount() {
        // given
        final Long projectId = 17L;
        final Integer oldAmount = 13;
        final Integer newAmount = 31;

        Measurement existingMeasurement = new Measurement();
        existingMeasurement.setBoard(new Board()); // needed to calculate hashCode
        existingMeasurement.setAmount(newAmount);

        Project testProject = new Project();
        Map<Measurement, Integer> measurements = new HashMap<>();
        measurements.put(existingMeasurement, oldAmount);
        testProject.setMeasurements(measurements);

        when(projectRepository.findById(projectId)).thenReturn(Optional.of(testProject));

        // when
        Project savedProject = projectService.addMeasurementToProject(projectId, existingMeasurement);

        // then
        assertThat(savedProject.getMeasurements().keySet()).containsOnly(existingMeasurement);
        assertThat(savedProject.getMeasurements().get(existingMeasurement)).isEqualTo(oldAmount + newAmount);
    }

    @Test
    public void givenExistingMeasurement_whenSubtractingFromProjectTooMany_throwException() {
        // given
        final Long projectId = 17L;
        final Integer oldAmount = 13;
        final Integer newAmount = -31;

        Measurement existingMeasurement = Measurement.builder()
                .board(new Board()) // needed to calculate hashCode
                .amount(newAmount)
                .build();
        new Measurement();

        Project testProject = new Project();
        Map<Measurement, Integer> measurements = new HashMap<>();
        measurements.put(existingMeasurement, oldAmount);
        testProject.setMeasurements(measurements);

        when(projectRepository.findById(projectId)).thenReturn(Optional.of(testProject));

        // when + then
        assertThatThrownBy(
                () -> projectService.addMeasurementToProject(projectId, existingMeasurement))
                .isInstanceOf(NegativeAmountException.class);
    }

    @Test
    public void givenSameMeasurementOfDifferentBoard_whenAddingToProject_addSeparately() {
        // given
        final Long projectId = 17L;
        final Integer width = 720;
        final Integer height = 360;
        final Integer oldAmount = 13;
        final Integer newAmount = 31;

        Board existingBoard = new Board();
        existingBoard.setName("existing board");
        Measurement existingMeasurement = Measurement.builder()
                .board(existingBoard)
                .height(height)
                .width(width)
                .build();

        Board newBoard = new Board();
        newBoard.setName("new board");
        Measurement newMeasurement = Measurement.builder()
                .board(newBoard) // different board than existingMeasurement
                .height(height) // same as existingMeasurement
                .width(width) // same as existingMeasurement
                .amount(newAmount)
                .build();

        Project testProject = new Project();
        Map<Measurement, Integer> measurements = new HashMap<>();
        measurements.put(existingMeasurement, oldAmount);
        testProject.setMeasurements(measurements);

        when(projectRepository.findById(projectId)).thenReturn(Optional.of(testProject));

        // when
        Project savedProject = projectService.addMeasurementToProject(projectId, newMeasurement);

        // then
        assertThat(savedProject.getMeasurements().size()).isEqualTo(2);
        assertThat(savedProject.getMeasurements().get(newMeasurement)).isEqualTo(newAmount);
    }

    @Test
    public void givenUpdatedDetails_whenSaving_changeToNewDetails() {
        // given
        final BigDecimal workerCost = BigDecimal.valueOf(7);
        final BigDecimal montageCost = BigDecimal.valueOf(13);
        final BigDecimal otherCosts = BigDecimal.valueOf(19);
        final BigDecimal totalCost = BigDecimal.valueOf(39);
        final Long projectId = 43L;


        ProjectDetails newDetails = ProjectDetails.builder()
                .workerCost(workerCost)
                .montageCost(montageCost)
                .otherCosts(otherCosts)
                .build();

        Project testProject = new Project();
        testProject.setProjectDetails(new ProjectDetails());

        when(projectRepository.findById(projectId)).thenReturn(Optional.of(testProject));

        // when
        projectService.updateProjectDetailsInProject(projectId, newDetails);

        // then
        assertThat(testProject.getProjectDetails().getWorkerCost()).isEqualTo(workerCost);
        assertThat(testProject.getTotalCost()).isEqualByComparingTo(totalCost);
    }

    // compute margin in List (no data && with data)
    @Test
    public void givenListOfProjects_whenMarginNotGiven_returnDash() {
        // given
        Project project1 = new Project();
        Project project2 = new Project();

        List<Project> testProjects = List.of(project1, project2);

        // when
        List<String> result = projectService.computeMarginList(testProjects);

        // then
        assertThat(result).contains("-", "-");

    }

    @Test
    public void givenListOfProjects_whenPreparingMarginForView_returnMarginInString() {
        // given
        final BigDecimal firstCost = BigDecimal.valueOf(100);
        final BigDecimal priceAtMargin25 = BigDecimal.valueOf(125);
        final BigDecimal marginOf25 = BigDecimal.valueOf(25);
        final BigDecimal secondCost = BigDecimal.valueOf(100);
        final BigDecimal priceAtMargin100 = BigDecimal.valueOf(200);
        final BigDecimal marginOf100 = BigDecimal.valueOf(100);

        Project project1 = Project.builder()
                .totalCost(firstCost)
                .price(priceAtMargin25)
                .margin(marginOf25)
                .build();

        Project project2 = Project.builder()
                .totalCost(secondCost)
                .price(priceAtMargin100)
                .margin(marginOf100)
                .build();

        List<Project> testProjects = List.of(project1, project2);

        // when
        List<String> result = projectService.computeMarginList(testProjects);

        // then
        assertThat(result).contains("25%", "100%");
    }

    // getBoardsDetailsByProject()
    @Test
    public void givenNoMeasurements_whenCreatingProjectView_returnEmptyStatus() {
        // given
        final Integer ofOne = 1;
        final Long projectId = 7L;
        final Long expectedBoardId = 0L;
        final String expectedName = "Brak płyt";
        Project testProject = new Project();

        when(projectRepository.findById(projectId)).thenReturn(Optional.of(testProject));

        // when
        List<BoardsByProjectResponseDto> result = projectService.getBoardsDetailsByProject(projectId);

        // then
        assertThat(result).hasSize(ofOne);
        BoardsByProjectResponseDto responseDto = result.stream().findFirst().get();

        assertThat(responseDto.getBoardId()).isEqualTo(expectedBoardId);
        assertThat(responseDto.getName()).isEqualTo(expectedName);
        assertThat(responseDto.getTotalArea()).isEqualTo(BigDecimal.ZERO);
        assertThat(responseDto.getTotalCost()).isEqualTo(BigDecimal.ZERO);
    }

    @Test
    public void givenMeasurementsOfSameBoard_whenCreatingProjectView_sumTheirAreas() {
        // given
        final Integer ofOne = 1;
        final Integer firstWidth = 100;
        final Integer secondWidth = 10;
        final Integer firstHeight = 10;
        final Integer secondHeight = 100;
        final BigDecimal pricePerM2 = BigDecimal.valueOf(10);
        final BigDecimal expectedArea = BigDecimal.valueOf(0.02);
        final String boardName = "nice board";

        Board board = Board.builder()
                .id(9L)
                .name(boardName)
                .pricePerM2(pricePerM2)
                .build();

        Measurement measurement1 = Measurement.builder()
                .width(firstWidth)
                .height(firstHeight)
                .board(board)
                .build();

        Measurement measurement2 = Measurement.builder()
                .width(secondWidth)
                .height(secondHeight)
                .board(board)
                .build();

        Map<Measurement, Integer> measurements = Map.of(measurement1, 7, measurement2, 13);

        Project testProject = new Project();
        testProject.setMeasurements(measurements);

        when(projectRepository.findById(any())).thenReturn(Optional.of(testProject));

        // when
        List<BoardsByProjectResponseDto> result = projectService.getBoardsDetailsByProject(1L);

        // then
        assertThat(result).hasSize(ofOne);
        BoardsByProjectResponseDto responseDto = result.stream().findFirst().get();
        assertThat(responseDto.getTotalArea()).isEqualByComparingTo(expectedArea);
    }

    @Test
    public void givenMeasurementsOfSameBoard_whenCreatingProjectView_sumTheirCost() {
        // given
        final Integer ofOne = 1;
        final Integer firstWidth = 100;
        final Integer secondWidth = 10;
        final Integer firstHeight = 10;
        final Integer secondHeight = 100;
        final BigDecimal pricePerM2 = BigDecimal.valueOf(10);
        final BigDecimal expectedCost = BigDecimal.valueOf(0.2);
        final String boardName = "nice board";

        Board board = Board.builder()
                .id(9L)
                .name(boardName)
                .pricePerM2(pricePerM2)
                .build();

        Measurement measurement1 = Measurement.builder()
                .width(firstWidth)
                .height(firstHeight)
                .board(board)
                .build();

        Measurement measurement2 = Measurement.builder()
                .width(secondWidth)
                .height(secondHeight)
                .board(board)
                .build();

        Map<Measurement, Integer> measurements = Map.of(measurement1, 7, measurement2, 13);

        Project testProject = new Project();
        testProject.setMeasurements(measurements);

        when(projectRepository.findById(any())).thenReturn(Optional.of(testProject));

        // when
        List<BoardsByProjectResponseDto> result = projectService.getBoardsDetailsByProject(1L);

        // then
        assertThat(result).hasSize(ofOne);
        BoardsByProjectResponseDto responseDto = result.stream().findFirst().get();
        assertThat(responseDto.getTotalCost()).isEqualByComparingTo(expectedCost);

    }
}