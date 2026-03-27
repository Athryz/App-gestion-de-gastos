package com.appgestor.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.appgestor.models.Movement;

public interface MovementRepository extends JpaRepository<Movement, Long> {
    List<Movement> findByUserId(Long userId);
}