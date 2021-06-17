package com.lewandowski.wycena3000.repository;

import com.lewandowski.wycena3000.entity.Board;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Set;

public interface BoardRepository extends JpaRepository<Board, Long> {

    @Query(value = "select distinct b.* from board b join measurement m on b.id = m.board_id join project_measurement pm on m.id = pm.measurement_id join project p on p.id = pm.project_id where p.id = ?1", nativeQuery = true)
    List<Board> findAllByProjectId(long projectId);

    @Query(value = "select b.id from measurement m right join board b on b.id = m.board_id where m.id is null", nativeQuery = true)
    Set<Long> enabledDeleteSet();

    List<Board> findAllByUserIdOrderByIdAsc(long userId);
}
