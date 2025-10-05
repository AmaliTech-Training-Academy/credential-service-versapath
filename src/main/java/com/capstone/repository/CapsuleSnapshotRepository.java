package com.capstone.repository;

import com.capstone.model.CapsuleSnapshot;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface CapsuleSnapshotRepository extends JpaRepository<CapsuleSnapshot, UUID> {
    Optional<CapsuleSnapshot> findByCapsuleId(UUID capsuleId);
}
