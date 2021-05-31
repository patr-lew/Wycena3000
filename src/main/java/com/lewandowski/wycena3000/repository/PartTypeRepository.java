package com.lewandowski.wycena3000.repository;
import com.lewandowski.wycena3000.entity.PartType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Set;

public interface PartTypeRepository extends JpaRepository<PartType, Long> {

    @Query(value = "select distinct pt.* from part_type pt join part p on pt.id = p.type_id join project_part pp on p.id = pp.part_id where pp.project_id = ?1", nativeQuery = true)
    List<PartType> findByProjectId(Long projectId);
}
