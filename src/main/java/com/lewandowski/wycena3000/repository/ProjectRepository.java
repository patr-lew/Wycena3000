package com.lewandowski.wycena3000.repository;

import com.lewandowski.wycena3000.entity.Project;
import org.springframework.data.jpa.repository.JpaRepository;


public interface ProjectRepository extends JpaRepository<Project, Long> {
}
