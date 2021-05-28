package com.lewandowski.wycena3000.repository;

import com.lewandowski.wycena3000.entity.BoardMeasurement;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Set;

public interface BoardMeasurementRepository extends JpaRepository<BoardMeasurement, Long> {

    @Query(value = "select bm.* from board_measurement bm join project_board pb on bm.id = pb.board_id where pb.project_id = ?1", nativeQuery = true)
    List<BoardMeasurement> findAllByProjectId(Long projectId);

    @Query(value = "select bm.* from board_measurement bm full outer join project_board pb on bm.id = pb.board_id where project_id is null", nativeQuery = true)
    Set<BoardMeasurement> findAllOrphans();
}
