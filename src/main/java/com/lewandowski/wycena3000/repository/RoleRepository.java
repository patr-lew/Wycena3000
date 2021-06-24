package com.lewandowski.wycena3000.repository;

import com.lewandowski.wycena3000.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoleRepository extends JpaRepository<Role, Integer> {
    Role findByName(String name);
}
