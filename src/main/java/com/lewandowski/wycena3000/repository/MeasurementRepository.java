package com.lewandowski.wycena3000.repository;

import com.lewandowski.wycena3000.entity.Measurement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Set;

public interface MeasurementRepository extends JpaRepository<Measurement, Long> {

    @Query(value = "select bm.* from measurement bm join project_measurement pm on bm.id = pm.measurement_id where pm.project_id = ?1", nativeQuery = true)
    List<Measurement> findAllByProjectId(Long projectId);

    @Query(value = "select bm.* from measurement bm full outer join project_measurement pm on bm.id = pm.measurement_id where project_id is null", nativeQuery = true)
    Set<Measurement> findAllOrphans();
}
