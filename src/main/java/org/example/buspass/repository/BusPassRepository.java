package org.example.buspass.repository;

import org.example.buspass.entity.BusPass;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BusPassRepository extends JpaRepository<BusPass, Long> {

    List<BusPass> findByUserId(Long userId);

    List<BusPass> findByStatus(String status);

    long countByStatus(String status);

    long countByUserId(Long userId);

    long countByUserIdAndStatus(Long userId, String status);

    List<BusPass>
    findByUser_NameContainingIgnoreCaseOrUser_EmailContainingIgnoreCaseOrUser_PhoneContaining(
            String name,
            String email,
            String phone
    );
}