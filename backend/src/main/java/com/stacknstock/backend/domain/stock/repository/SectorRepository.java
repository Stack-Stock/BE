package com.stacknstock.backend.domain.stock.repository;

import com.stacknstock.backend.domain.stock.entity.Sector;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SectorRepository extends JpaRepository<Sector, Long> {
}
