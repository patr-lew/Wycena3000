package com.lewandowski.wycena3000.repository;

import com.lewandowski.wycena3000.entity.FurniturePart;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface FurniturePartRepository extends JpaRepository<FurniturePart, Long> {
}
