package com.lewandowski.wycena3000.repository;

import com.lewandowski.wycena3000.entity.Project;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;


public interface ProjectRepository extends JpaRepository<Project, Long> {

    List<Project> findAllByOrderByCreatedAsc();

    List<Project> findAllByUserIdOrderByCreatedAsc(Long userId);
}
