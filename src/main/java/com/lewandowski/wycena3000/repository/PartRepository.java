package com.lewandowski.wycena3000.repository;

import com.lewandowski.wycena3000.entity.Part;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Set;

public interface PartRepository extends JpaRepository<com.lewandowski.wycena3000.entity.Part, Long> {

    @Query(value = "select p.* from project_part pp right join part p on pp.part_id = p.id where pp.part_id is null", nativeQuery = true)
    Set<Long> enabledDeleteSet();

    List<Part> findAllByUserIdOrderByIdAsc(long id);
}
