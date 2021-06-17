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
import java.util.*;

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
        final BigDecimal ZERO_COST = BigDecimal.ZERO;
        Project testProject = new Project();
        when(projectRepository.save(any())).thenReturn(testProject);

        // when
        Project savedProject = projectService.saveAndUpdate(testProject);

        // then
        assertThat(savedProject.getTotalCost()).isEqualTo(ZERO_COST);
    }

    @Test
    public void givenProjectDetailCosts_whenSavingProject_updateTotalCost() {
        // given
        final BigDecimal WORKERS_COST = BigDecimal.valueOf(1);
        final BigDecimal MONTAGE_COST = BigDecimal.valueOf(2);
        final BigDecimal OTHER_COST = BigDecimal.valueOf(4);
        final BigDecimal TOTAL_COST = BigDecimal.valueOf(7);

        ProjectDetails projectDetails = new ProjectDetails();
        Project testProject = new Project();
        testProject.setProjectDetails(projectDetails);

        projectDetails.setWorkerCost(WORKERS_COST);
        projectDetails.setMontageCost(MONTAGE_COST);
        projectDetails.setOtherCosts(OTHER_COST);

        when(projectRepository.save(any())).thenReturn(testProject);

        // when
        Project savedProject = projectService.saveAndUpdate(testProject);

        // then
        assertThat(savedProject.getTotalCost()).isEqualTo(TOTAL_COST);
    }

    @Test
    public void givenMeasurementsCosts_whenSavingProject_updateTotalCost() {
        // given
        final Integer FIRST_AMOUNT = 10;
        final Integer SECOND_AMOUNT = 100;
        final BigDecimal PRICE_PER_M2 = BigDecimal.TEN;
        final BigDecimal TOTAL_COST = BigDecimal.valueOf(1.1);

        Board board = new Board();
        board.setPricePerM2(PRICE_PER_M2);

        Measurement measurement1 = new Measurement();
        measurement1.setBoard(board);
        measurement1.setHeight(100);
        measurement1.setWidth(100);

        Measurement measurement2 = new Measurement();
        measurement2.setBoard(board);
        measurement2.setHeight(10);
        measurement2.setWidth(10);

        Map<Measurement, Integer> testMeasurements = new HashMap<>();
        testMeasurements.put(measurement1, FIRST_AMOUNT);
        testMeasurements.put(measurement2, SECOND_AMOUNT);

        Project testProject = new Project();
        testProject.setMeasurements(testMeasurements);

        when(projectRepository.save(any())).thenReturn(testProject);

        // when
        Project savedProject = projectService.saveAndUpdate(testProject);

        // then
        assertThat(savedProject.getTotalCost()).isEqualByComparingTo(TOTAL_COST);

    }

    @Test
    public void givenPartCosts_whenSavingProject_updateTotalCost() {
        // given
        final Integer FIRST_AMOUNT = 10;
        final Integer SECOND_AMOUNT = 100;
        final BigDecimal FIRST_PRICE = BigDecimal.valueOf(100);
        final BigDecimal SECOND_PRICE = BigDecimal.valueOf(10);
        final BigDecimal TOTAL_COST = BigDecimal.valueOf(2000);

        Part part1 = new Part();
        part1.setPrice(FIRST_PRICE);
        Part part2 = new Part();
        part2.setPrice(SECOND_PRICE);

        Map<Part, Integer> testParts = new HashMap<>();
        testParts.put(part1, FIRST_AMOUNT);
        testParts.put(part2, SECOND_AMOUNT);

        Project testProject = new Project();
        testProject.setParts(testParts);

        when(projectRepository.save(any())).thenReturn(testProject);

        // when
        Project savedProject = projectService.saveAndUpdate(testProject);

        // then
        assertThat(savedProject.getTotalCost()).isEqualByComparingTo(TOTAL_COST);
    }

    // save() -> calculateMargin()
    @Test
    public void givenUpdatedPrice_whenSavingProject_updateCalculatedProjectMargin() {
        // given
        final BigDecimal TOTAL_COST = BigDecimal.valueOf(100);
        final BigDecimal PROJECT_PRICE = BigDecimal.valueOf(200);
        final BigDecimal MARGIN = BigDecimal.valueOf(100);

        ProjectDetails sourceOfCosts = new ProjectDetails();
        sourceOfCosts.setOtherCosts(TOTAL_COST);
        Project testProject = new Project();
        testProject.setProjectDetails(sourceOfCosts);
        testProject.setPrice(PROJECT_PRICE);

        when(projectRepository.save(any())).thenReturn(testProject);

        // when
        Project savedProject = projectService.saveAndUpdate(testProject);

        // then
        assertThat(savedProject.getMargin()).isEqualByComparingTo(MARGIN);
    }

    @Test
    public void givenEmptyProject_whenSavingProject_setMarginToZero() {
        // given
        final BigDecimal MARGIN = BigDecimal.ZERO;
        Project testProject = new Project();

        when(projectRepository.save(any())).thenReturn(testProject);

        // when
        Project savedProject = projectService.saveAndUpdate(testProject);

        // then
        assertThat(savedProject.getMargin()).isEqualByComparingTo(MARGIN);
    }

    // addPartToProject
    @Test
    public void givenNewPart_whenAddingToProject_addPart() {
        // given
        final Long PROJECT_ID = 7L;
        final Long PART_ID = 11L;
        final Integer AMOUNT = 23;
        final String PART_NAME = "Part name";

        Part newPart = new Part();
        newPart.setId(PART_ID);
        newPart.setName(PART_NAME);

        Project testProject = new Project();
        testProject.setId(PROJECT_ID);
        testProject.setParts(new HashMap<>());

        AddPartToProjectRequestDto dto = new AddPartToProjectRequestDto(PROJECT_ID, PART_ID, AMOUNT);

        when(partRepository.findById(any())).thenReturn(Optional.of(newPart));
        when(projectRepository.findById(any())).thenReturn(Optional.of(testProject));

        // when
        Project savedProject = projectService.addPartToProject(dto);

        // then
        Part savedPart = savedProject.getParts()
                .keySet().stream()
                .findFirst()
                .get();
        assertThat(savedPart.getName()).isEqualTo(PART_NAME);
        assertThat(savedProject.getParts().get(savedPart)).isEqualTo(AMOUNT);
    }

    @Test
    public void givenExistingPart_whenAddingToProject_updatePartsAmount() {
        // given
        final Long PART_ID = 7L;
        final Long PROJECT_ID = 11L;
        final Integer OLD_AMOUNT = 5;
        final Integer NEW_AMOUNT = 20;
        final String PART_NAME = "Part name";

        Part existingPart = new Part();
        existingPart.setId(PART_ID);
        existingPart.setName(PART_NAME);

        Project testProject = new Project();
        testProject.setId(PROJECT_ID);
        Map<Part, Integer> parts = new HashMap<>();
        parts.put(existingPart, OLD_AMOUNT);
        testProject.setParts(parts);

        AddPartToProjectRequestDto dto = new AddPartToProjectRequestDto(PROJECT_ID, PART_ID, NEW_AMOUNT);

        when(partRepository.findById(PART_ID)).thenReturn(Optional.of(existingPart));
        when(projectRepository.findById(PROJECT_ID)).thenReturn(Optional.of(testProject));

        // when
        Project savedProject = projectService.addPartToProject(dto);

        // then
        assertThat(savedProject.getParts().size()).isEqualTo(1);
        Part savedPart = savedProject.getParts().keySet().stream().findFirst().get();

        assertThat(savedProject.getParts().get(savedPart)).isEqualTo(OLD_AMOUNT + NEW_AMOUNT);
    }

    @Test
    public void givenExistingPart_whenSubtractingFromProjectTooMany_throwException() {
        // given
        final Long PART_ID = 7L;
        final Long PROJECT_ID = 11L;
        final Integer OLD_AMOUNT = 5;
        final Integer NEW_AMOUNT = -20;
        final String PART_NAME = "Part name";

        Part existingPart = new Part();
        existingPart.setId(PART_ID);
        existingPart.setName(PART_NAME);

        Project testProject = new Project();
        testProject.setId(PROJECT_ID);
        Map<Part, Integer> parts = new HashMap<>();
        parts.put(existingPart, OLD_AMOUNT);
        testProject.setParts(parts);

        AddPartToProjectRequestDto dto = new AddPartToProjectRequestDto(PROJECT_ID, PART_ID, NEW_AMOUNT);

        when(partRepository.findById(PART_ID)).thenReturn(Optional.of(existingPart));
        when(projectRepository.findById(PROJECT_ID)).thenReturn(Optional.of(testProject));

        // when + then
        assertThatThrownBy(
                () -> projectService.addPartToProject(dto))
                .isInstanceOf(NegativeAmountException.class);

    }

    // addBoardMeasurementToProject
    @Test
    public void givenNewMeasurement_whenAddingToProject_AddMeasurement() {
        // given
        final Long PROJECT_ID = 17L;
        final Integer AMOUNT = 13;

        Measurement addedMeasurement = new Measurement();
        addedMeasurement.setAmount(AMOUNT);

        Project testProject = new Project();
        testProject.setMeasurements(new HashMap<>());

        when(projectRepository.findById(PROJECT_ID)).thenReturn(Optional.of(testProject));

        // when
        Project savedProject = projectService.addMeasurementToProject(PROJECT_ID, addedMeasurement);

        // then
        assertThat(savedProject.getMeasurements().keySet()).containsOnly(addedMeasurement);
        assertThat(savedProject.getMeasurements().get(addedMeasurement)).isEqualTo(AMOUNT);
    }

    @Test
    public void givenExistingMeasurement_whenAddingToProject_updateMeasurementsAmount() {
        // given
        final Long PROJECT_ID = 17L;
        final Integer OLD_AMOUNT = 13;
        final Integer NEW_AMOUNT = 31;

        Measurement existingMeasurement = new Measurement();
        existingMeasurement.setBoard(new Board()); // needed to calculate hashCode
        existingMeasurement.setAmount(NEW_AMOUNT);

        Project testProject = new Project();
        Map<Measurement, Integer> measurements = new HashMap<>();
        measurements.put(existingMeasurement, OLD_AMOUNT);
        testProject.setMeasurements(measurements);

        when(projectRepository.findById(PROJECT_ID)).thenReturn(Optional.of(testProject));

        // when
        Project savedProject = projectService.addMeasurementToProject(PROJECT_ID, existingMeasurement);

        // then
        assertThat(savedProject.getMeasurements().keySet()).containsOnly(existingMeasurement);
        assertThat(savedProject.getMeasurements().get(existingMeasurement)).isEqualTo(OLD_AMOUNT + NEW_AMOUNT);
    }

    @Test
    public void givenExistingMeasurement_whenSubtractingFromProjectTooMany_throwException() {
        // given
        final Long PROJECT_ID = 17L;
        final Integer OLD_AMOUNT = 13;
        final Integer NEW_AMOUNT = -31;

        Measurement existingMeasurement = new Measurement();
        existingMeasurement.setBoard(new Board()); // needed to calculate hashCode
        existingMeasurement.setAmount(NEW_AMOUNT);

        Project testProject = new Project();
        Map<Measurement, Integer> measurements = new HashMap<>();
        measurements.put(existingMeasurement, OLD_AMOUNT);
        testProject.setMeasurements(measurements);

        when(projectRepository.findById(PROJECT_ID)).thenReturn(Optional.of(testProject));

        // when + then
        assertThatThrownBy(
                () -> projectService.addMeasurementToProject(PROJECT_ID, existingMeasurement))
                .isInstanceOf(NegativeAmountException.class);
    }
    @Test
    public void givenSameMeasurementOfDifferentBoard_whenAddingToProject_addSeparately() {
        // given
        final Long PROJECT_ID = 17L;
        final Integer WIDTH = 720;
        final Integer HEIGHT = 360;
        final Integer OLD_AMOUNT = 13;
        final Integer NEW_AMOUNT = 31;

        Board existingBoard = new Board();
        existingBoard.setName("existing board");
        Measurement existingMeasurement = new Measurement();
        existingMeasurement.setBoard(existingBoard); // needed to calculate hashCode
        existingMeasurement.setHeight(HEIGHT);
        existingMeasurement.setWidth(WIDTH);

        Board newBoard = new Board();
        newBoard.setName("new board");
        Measurement newMeasurement = new Measurement();
        newMeasurement.setBoard(newBoard); // different board than above
        newMeasurement.setHeight(HEIGHT); // same as above
        newMeasurement.setWidth(WIDTH); // same as above
        newMeasurement.setAmount(NEW_AMOUNT);

        Project testProject = new Project();
        Map<Measurement, Integer> measurements = new HashMap<>();
        measurements.put(existingMeasurement, OLD_AMOUNT);
        testProject.setMeasurements(measurements);

        when(projectRepository.findById(PROJECT_ID)).thenReturn(Optional.of(testProject));

        // when
        Project savedProject = projectService.addMeasurementToProject(PROJECT_ID, newMeasurement);

        // then
        assertThat(savedProject.getMeasurements().size()).isEqualTo(2);
        assertThat(savedProject.getMeasurements().get(newMeasurement)).isEqualTo(NEW_AMOUNT);
    }

    // updateProjectDetailsInProject()
    @Test
    public void givenUpdatedDetails_whenSaving_changeToNewDetails() {
        // given
        final BigDecimal WORKER_COST = BigDecimal.valueOf(7);
        final BigDecimal MONTAGE_COST = BigDecimal.valueOf(13);
        final BigDecimal OTHER_COSTS = BigDecimal.valueOf(19);
        final BigDecimal TOTAL_COST = BigDecimal.valueOf(39);
        final Long PROJECT_ID = 43L;

        ProjectDetails newDetails = new ProjectDetails();
        newDetails.setWorkerCost(WORKER_COST);
        newDetails.setMontageCost(MONTAGE_COST);
        newDetails.setOtherCosts(OTHER_COSTS);

        Project testProject = new Project();
        testProject.setProjectDetails(new ProjectDetails());

        when(projectRepository.findById(PROJECT_ID)).thenReturn(Optional.of(testProject));

        // when
        projectService.updateProjectDetailsInProject(PROJECT_ID, newDetails);

        // then
        assertThat(testProject.getProjectDetails().getWorkerCost()).isEqualTo(WORKER_COST);
        assertThat(testProject.getTotalCost()).isEqualByComparingTo(TOTAL_COST);
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
        final BigDecimal FIRST_COST = BigDecimal.valueOf(100);
        final BigDecimal PRICE_AT_MARGIN_25 = BigDecimal.valueOf(125);
        final BigDecimal MARGIN_OF_25 = BigDecimal.valueOf(25);
        final BigDecimal SECOND_COST = BigDecimal.valueOf(100);
        final BigDecimal PRICE_AT_MARGIN_100 = BigDecimal.valueOf(200);
        final BigDecimal MARGIN_OF_100 = BigDecimal.valueOf(100);

        Project project1 = new Project();
        project1.setTotalCost(FIRST_COST);
        project1.setPrice(PRICE_AT_MARGIN_25);
        project1.setMargin(MARGIN_OF_25);

        Project project2 = new Project();
        project2.setTotalCost(SECOND_COST);
        project2.setPrice(PRICE_AT_MARGIN_100);
        project2.setMargin(MARGIN_OF_100);

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
        final Integer OF_ONE = 1;
        final Long PROJECT_ID = 7L;
        final Long EXPECTED_BOARD_ID = 0L;
        final String EXPECTED_NAME = "Brak płyt";
        Project testProject = new Project();

        when(projectRepository.findById(PROJECT_ID)).thenReturn(Optional.of(testProject));

        // when
        List<BoardsByProjectResponseDto> result = projectService.getBoardsDetailsByProject(PROJECT_ID);

        // then
        assertThat(result).hasSize(OF_ONE);
        BoardsByProjectResponseDto responseDto = result.stream().findFirst().get();

        assertThat(responseDto.getBoardId()).isEqualTo(EXPECTED_BOARD_ID);
        assertThat(responseDto.getName()).isEqualTo(EXPECTED_NAME);
        assertThat(responseDto.getTotalArea()).isEqualTo(BigDecimal.ZERO);
        assertThat(responseDto.getTotalCost()).isEqualTo(BigDecimal.ZERO);
    }

    @Test
    public void givenMeasurementsOfSameBoard_whenCreatingProjectView_sumTheirAreas() {
        // given
        final Integer OF_ONE = 1;
        final Integer FIRST_WIDTH = 100;
        final Integer SECOND_WIDTH = 10;
        final Integer FIRST_HEIGHT = 10;
        final Integer SECOND_HEIGHT = 100;
        final BigDecimal PRICE_PER_M2 = BigDecimal.valueOf(10);
        final BigDecimal EXPECTED_AREA = BigDecimal.valueOf(0.02);
        final String BOARD_NAME = "nice board";

        Board board = new Board();
        board.setId(9L);
        board.setName(BOARD_NAME);
        board.setPricePerM2(PRICE_PER_M2);

        Measurement measurement1 = new Measurement();
        measurement1.setWidth(FIRST_WIDTH);
        measurement1.setHeight(FIRST_HEIGHT);
        measurement1.setBoard(board);

        Measurement measurement2 = new Measurement();
        measurement2.setWidth(SECOND_WIDTH);
        measurement2.setHeight(SECOND_HEIGHT);
        measurement2.setBoard(board);

        Map<Measurement, Integer> measurements = Map.of(measurement1, 7, measurement2, 13);

        Project testProject = new Project();
        testProject.setMeasurements(measurements);

        when(projectRepository.findById(any())).thenReturn(Optional.of(testProject));

        // when
        List<BoardsByProjectResponseDto> result = projectService.getBoardsDetailsByProject(1L);

        // then
        assertThat(result).hasSize(OF_ONE);
        BoardsByProjectResponseDto responseDto = result.stream().findFirst().get();
        assertThat(responseDto.getTotalArea()).isEqualByComparingTo(EXPECTED_AREA);
    }

    @Test
    public void givenMeasurementsOfSameBoard_whenCreatingProjectView_sumTheirCost() {
        // given
       final Integer OF_ONE = 1;
        final Integer FIRST_WIDTH = 100;
        final Integer SECOND_WIDTH = 10;
        final Integer FIRST_HEIGHT = 10;
        final Integer SECOND_HEIGHT = 100;
        final BigDecimal PRICE_PER_M2 = BigDecimal.valueOf(10);
        final BigDecimal EXPECTED_COST = BigDecimal.valueOf(0.2);
        final String BOARD_NAME = "nice board";

        Board board = new Board();
        board.setId(9L);
        board.setName(BOARD_NAME);
        board.setPricePerM2(PRICE_PER_M2);

        Measurement measurement1 = new Measurement();
        measurement1.setWidth(FIRST_WIDTH);
        measurement1.setHeight(FIRST_HEIGHT);
        measurement1.setBoard(board);

        Measurement measurement2 = new Measurement();
        measurement2.setWidth(SECOND_WIDTH);
        measurement2.setHeight(SECOND_HEIGHT);
        measurement2.setBoard(board);

        Map<Measurement, Integer> measurements = Map.of(measurement1, 7, measurement2, 13);

        Project testProject = new Project();
        testProject.setMeasurements(measurements);

        when(projectRepository.findById(any())).thenReturn(Optional.of(testProject));

        // when
        List<BoardsByProjectResponseDto> result = projectService.getBoardsDetailsByProject(1L);

        // then
        assertThat(result).hasSize(OF_ONE);
        BoardsByProjectResponseDto responseDto = result.stream().findFirst().get();
        assertThat(responseDto.getTotalCost()).isEqualByComparingTo(EXPECTED_COST);

    }
}