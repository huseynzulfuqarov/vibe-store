package com.example.vibe_store.repository;

import com.example.vibe_store.entity.employee.EmployeeMonthlyBonus;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EmployeeMonthlyBonusRepository extends JpaRepository<EmployeeMonthlyBonus, Long> {
}