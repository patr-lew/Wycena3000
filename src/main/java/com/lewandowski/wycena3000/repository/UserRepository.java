package com.lewandowski.wycena3000.repository;

import com.lewandowski.wycena3000.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
    User findByUsername(String username);
}
