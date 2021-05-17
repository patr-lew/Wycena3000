package com.lewandowski.wycena3000.service;

import com.lewandowski.wycena3000.entity.Project;
import com.lewandowski.wycena3000.repository.ProjectRepository;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.List;

@Service
@Transactional
public class ProjectService {

    private final ProjectRepository projectRepository;

    public ProjectService(ProjectRepository projectRepository) {
        this.projectRepository = projectRepository;
    }

    public List<Project> findAll() {
        return projectRepository.findAll();
    }


}
