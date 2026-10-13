package com.carpooling.auth.repository;

import com.carpooling.auth.entity.User;
import com.carpooling.auth.entity.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);
    long countByIsActiveTrue();
    long countByIsBlockedTrue();
    List<User> findByRolesContaining(UserRole role);
}
