package com.example.vibe_store.repository;

import com.example.vibe_store.entity.User;
import com.example.vibe_store.entity.employee.Employee;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsername(String username);

    Optional<User> findByEmployee(Employee employee);

    Optional<User> findByEmail(String email);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);
}
