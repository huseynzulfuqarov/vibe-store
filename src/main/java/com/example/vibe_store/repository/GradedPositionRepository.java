package com.example.vibe_store.repository;

import com.example.vibe_store.entity.grade.GradedPosition;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GradedPositionRepository extends JpaRepository<GradedPosition, Integer> {
}