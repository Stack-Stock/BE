package com.stacknstock.backend.domain.action.repository;

import com.stacknstock.backend.domain.action.entity.Action;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ActionRepository extends JpaRepository<Action, Long> {
}
