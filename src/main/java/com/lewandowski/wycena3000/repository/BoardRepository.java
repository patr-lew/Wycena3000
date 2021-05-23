package com.lewandowski.wycena3000.repository;

import com.lewandowski.wycena3000.entity.Board;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface BoardRepository extends JpaRepository<Board, Long> {

    @Query(value = "select distinct b.* from board b join board_measurement bm on b.id = bm.board_id join project_board pb on bm.id = pb.board_id join project p on p.id = pb.project_id where p.id = ?1", nativeQuery = true)
    List<Board> findAllByProjectId(long projectId);
}
