package com.stacknstock.backend.domain.scenario.repository;

import com.stacknstock.backend.domain.scenario.entity.ScenarioDay;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ScenarioDayRepository extends JpaRepository<ScenarioDay, Long> {
}
