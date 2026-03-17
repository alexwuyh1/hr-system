package com.example.hr.repository;

import com.example.hr.model.User;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * User repository for login and registration lookups.
 */
public interface UserRepository extends JpaRepository<User, Long> {
  Optional<User> findByUsername(String username);
}
