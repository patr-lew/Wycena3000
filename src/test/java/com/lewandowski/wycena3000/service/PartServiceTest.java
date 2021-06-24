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
        final Long projectId = 1L;
        final Long oldPartId = 5L;
        final Long newPartId = 10L;
        final Integer amountOfParts = 15;


        Part oldPart = new Part();
        oldPart.setId(oldPartId);
        oldPart.setName("Old part"); // hash doesn't include ID

        Part newPart = new Part();
        newPart.setId(newPartId);
        newPart.setName("New part");

        Project testProject = new Project();
        testProject.setParts(new HashMap<>());
        testProject.getParts().put(oldPart, amountOfParts);

        PartChangeRequestDto dto = new PartChangeRequestDto(projectId, oldPartId, newPartId);

        when(projectRepository.findById(projectId)).thenReturn(Optional.of(testProject));
        when(partRepository.findById(oldPartId)).thenReturn(Optional.of(oldPart));
        when(partRepository.findById(newPartId)).thenReturn(Optional.of(newPart));

        // when
        partService.changePartInProject(dto);

        // then
        assertThat(testProject.getParts().keySet()).containsOnly(newPart);
        assertThat(testProject.getParts().get(newPart)).isEqualTo(amountOfParts);
    }

    @Test
    public void whenProjectSwitchesPartToExistingPart_thenPartIsUpdated() {
        // given
        final Long projectId = 1L;
        final Long oldPartId = 5L;
        final Long newPartId = 10L;
        final Integer amountOfOldParts = 15;
        final Integer amountOfNewParts = 25;
        final Integer amountOfAllParts = amountOfOldParts + amountOfNewParts;


        Part oldPart = new Part();
        oldPart.setId(oldPartId);
        oldPart.setName("Old part"); // hash doesn't include ID

        Part newPart = new Part();
        newPart.setId(newPartId);
        newPart.setName("New part");

        Project testProject = new Project();
        testProject.setParts(new HashMap<>());
        testProject.getParts().put(oldPart, amountOfOldParts);
        testProject.getParts().put(newPart, amountOfNewParts);

        when(projectRepository.findById(projectId)).thenReturn(Optional.of(testProject));
        when(partRepository.findById(oldPartId)).thenReturn(Optional.of(oldPart));
        when(partRepository.findById(newPartId)).thenReturn(Optional.of(newPart));

        PartChangeRequestDto dto = new PartChangeRequestDto(projectId, oldPartId, newPartId);

        // when
        partService.changePartInProject(dto);

        // then
        assertThat(testProject.getParts().keySet()).containsOnly(newPart);
        assertThat(testProject.getParts().get(newPart)).isEqualTo(amountOfAllParts);
    }

}