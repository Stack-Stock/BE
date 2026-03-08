package com.stacknstock.backend.domain.user.repository;

import com.stacknstock.backend.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
}
