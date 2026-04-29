package com.example.vibe_store.repository;

import com.example.vibe_store.entity.employee.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface EmployeeRepository extends JpaRepository<Employee, Integer> {

  boolean existsByEmail(String email);

  @Query("SELECT e FROM Employee e WHERE " +
          "LOWER(CONCAT(e.firstName, ' ', e.lastName)) = LOWER(:fullName) OR " +
          "LOWER(CONCAT(e.lastName, ' ', e.firstName)) = LOWER(:fullName) OR " +
          "LOWER(e.firstName) = LOWER(:fullName) OR " +
          "LOWER(e.lastName) = LOWER(:fullName)")
  List<Employee> findByFullNameCombinations(@Param("fullName") String fullName);
}