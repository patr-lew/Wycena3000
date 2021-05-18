package com.lewandowski.wycena3000.repository;

import com.lewandowski.wycena3000.entity.BoardMeasurement;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WoodenBoardRepository extends JpaRepository<BoardMeasurement, Long> {
}
