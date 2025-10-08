package com.capstone.repository;

import com.capstone.model.IssuedBadge;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface IssuedBadgeRepository extends JpaRepository<IssuedBadge, UUID> {
    boolean existsByUserSnapshotUserIdAndCapsuleSnapshotCapsuleId(UUID userId, UUID capsuleId);
    Optional<IssuedBadge> findByUserSnapshotUserIdAndCapsuleSnapshotCapsuleId(UUID userId, UUID capsuleId);
}
