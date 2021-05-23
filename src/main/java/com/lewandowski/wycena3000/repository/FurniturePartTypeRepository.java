package com.lewandowski.wycena3000.repository;
import com.lewandowski.wycena3000.entity.FurniturePartType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface FurniturePartTypeRepository extends JpaRepository<FurniturePartType, Long> {

    @Query(value = "select distinct pt.* from part_type pt join part p on pt.id = p.type_id join project_part pp on p.id = pp.part_id where pp.project_id = ?1", nativeQuery = true)
    List<FurniturePartType> findByProjectId(Long projectId);
}
