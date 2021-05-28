package com.lewandowski.wycena3000.repository;

import com.lewandowski.wycena3000.entity.FurniturePart;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Set;

public interface FurniturePartRepository extends JpaRepository<FurniturePart, Long> {

    @Query(value = "select p.* from project_part pp right join part p on pp.part_id = p.id where pp.part_id is null", nativeQuery = true)
    Set<Long> enabledDeleteSet();
}
