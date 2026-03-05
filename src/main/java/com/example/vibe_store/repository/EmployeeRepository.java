package com.example.vibe_store.repository;

import com.example.vibe_store.entity.employee.Employee;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EmployeeRepository extends JpaRepository<Employee, Integer> {
  boolean existsByEmail(String email);
}