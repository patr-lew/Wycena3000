package com.lewandowski.wycena3000.service;

import com.lewandowski.wycena3000.dto.PartChangeRequestDto;
import com.lewandowski.wycena3000.entity.Part;
import com.lewandowski.wycena3000.entity.Project;
import com.lewandowski.wycena3000.repository.PartRepository;
import com.lewandowski.wycena3000.repository.PartTypeRepository;
import com.lewandowski.wycena3000.repository.ProjectRepository;
import static org.assertj.core.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashMap;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PartServiceTest {

    @Mock
    private PartTypeRepository partTypeRepository;
    @Mock
    private PartRepository partRepository;
    @Mock
    private ProjectRepository projectRepository;
    private PartService partService;

    @BeforeEach
    void setUp() {
        partService = new PartService(partTypeRepository, partRepository, projectRepository);
    }

    @Test
    public void whenProjectSwitchesPartToNewPart_thenPartIsChanged() {
        // given
        final Long PROJECT_ID = 1L;
        final Long OLD_PART_ID = 5L;
        final Long NEW_PART_ID = 10L;
        final Integer AMOUNT_OF_PARTS = 15;


        Part oldPart = new Part();
        oldPart.setId(OLD_PART_ID);
        oldPart.setName("Old part"); // hash doesn't include ID

        Part newPart = new Part();
        newPart.setId(NEW_PART_ID);
        newPart.setName("New part");

        Project testProject = new Project();
        testProject.setParts(new HashMap<>());
        testProject.getParts().put(oldPart, AMOUNT_OF_PARTS);

        when(projectRepository.findById(PROJECT_ID)).thenReturn(Optional.of(testProject));
        when(partRepository.findById(OLD_PART_ID)).thenReturn(Optional.of(oldPart));
        when(partRepository.findById(NEW_PART_ID)).thenReturn(Optional.of(newPart));

        PartChangeRequestDto dto = new PartChangeRequestDto(PROJECT_ID, OLD_PART_ID, NEW_PART_ID);

        // when
        partService.changePartInProject(dto);

        // then
        assertThat(testProject.getParts().keySet()).containsOnly(newPart);
        assertThat(testProject.getParts().get(newPart)).isEqualTo(AMOUNT_OF_PARTS);
    }

    @Test
    public void whenProjectSwitchesPartToExistingPart_thenPartIsUpdated() {
        // given
        final Long PROJECT_ID = 1L;
        final Long OLD_PART_ID = 5L;
        final Long NEW_PART_ID = 10L;
        final Integer AMOUNT_OF_OLD_PARTS = 15;
        final Integer AMOUNT_OF_NEW_PARTS = 25;
        final Integer AMOUNT_OF_ALL_PARTS = AMOUNT_OF_OLD_PARTS + AMOUNT_OF_NEW_PARTS;


        Part oldPart = new Part();
        oldPart.setId(OLD_PART_ID);
        oldPart.setName("Old part"); // hash doesn't include ID

        Part newPart = new Part();
        newPart.setId(NEW_PART_ID);
        newPart.setName("New part");

        Project testProject = new Project();
        testProject.setParts(new HashMap<>());
        testProject.getParts().put(oldPart, AMOUNT_OF_OLD_PARTS);
        testProject.getParts().put(newPart, AMOUNT_OF_NEW_PARTS);

        when(projectRepository.findById(PROJECT_ID)).thenReturn(Optional.of(testProject));
        when(partRepository.findById(OLD_PART_ID)).thenReturn(Optional.of(oldPart));
        when(partRepository.findById(NEW_PART_ID)).thenReturn(Optional.of(newPart));

        PartChangeRequestDto dto = new PartChangeRequestDto(PROJECT_ID, OLD_PART_ID, NEW_PART_ID);

        // when
        partService.changePartInProject(dto);

        // then
        assertThat(testProject.getParts().keySet()).containsOnly(newPart);
        assertThat(testProject.getParts().get(newPart)).isEqualTo(AMOUNT_OF_ALL_PARTS);
    }

}