package com.lewandowski.wycena3000.repository;

import com.lewandowski.wycena3000.entity.BoardMeasurement;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface BoardMeasurementRepository extends JpaRepository<BoardMeasurement, Long> {

    @Query(value = "select bm.* from board_measurement bm join project_board pb on bm.id = pb.board_id where pb.project_id = ?1", nativeQuery = true)
    List<BoardMeasurement> findAllByProjectId(Long projectId);
}
