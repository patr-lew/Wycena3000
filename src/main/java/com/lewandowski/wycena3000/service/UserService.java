package com.lewandowski.wycena3000.service;

import com.lewandowski.wycena3000.entity.User;
import com.lewandowski.wycena3000.repository.UserRepository;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User findByUsername(String username) {
        return userRepository.findByUsername(username);
    }
}
