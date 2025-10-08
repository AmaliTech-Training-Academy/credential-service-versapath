package com.capstone.repository;

import com.capstone.model.IssuedBadge;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface IssuedBadgeRepository extends JpaRepository<IssuedBadge, UUID> {
    boolean existsByUserSnapshotUserIdAndCapsuleSnapshotCapsuleId(UUID userId, UUID capsuleId);
    Optional<IssuedBadge> findByUserSnapshotUserIdAndCapsuleSnapshotCapsuleId(UUID userId, UUID capsuleId);

    @Query("""
             SELECT DISTINCT ib FROM IssuedBadge ib
             JOIN FETCH ib.userSnapshot us
             JOIN FETCH ib.capsuleSnapshot cs
             JOIN FETCH ib.issuer i
             LEFT JOIN FETCH cs.capsuleAtomMappings cam
             LEFT JOIN FETCH cam.skillAtom sa
             WHERE us.userId = :learnerId
             ORDER BY ib.issuedOn DESC, ib.createdAt DESC
             """)
    Page<IssuedBadge> findByLearnerIdWithDetails(@Param("learnerId") UUID learnerId, Pageable pageable);

    @Query("""
             SELECT COUNT(DISTINCT ib) FROM IssuedBadge ib
             JOIN ib.userSnapshot us
             WHERE us.userId = :learnerId
             """)
    long countByLearnerId(@Param("learnerId") UUID learnerId);

    @Query("""
             SELECT ib FROM IssuedBadge ib
             JOIN FETCH ib.userSnapshot us
             JOIN FETCH ib.capsuleSnapshot cs
             JOIN FETCH ib.issuer i
             LEFT JOIN FETCH cs.capsuleAtomMappings cam
             LEFT JOIN FETCH cam.skillAtom sa
             WHERE ib.verificationCode = :verificationCode
             """)
    Optional<IssuedBadge> findByVerificationCodeWithDetails(@Param("verificationCode") String verificationCode);
}
