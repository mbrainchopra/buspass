package org.example.buspass.repository;

import org.example.buspass.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    List<User> findByNameContainingIgnoreCaseOrEmailContainingIgnoreCaseOrPhoneContaining(
            String name,
            String email,
            String phone
    );

    List<User> findByRole(String role);
}