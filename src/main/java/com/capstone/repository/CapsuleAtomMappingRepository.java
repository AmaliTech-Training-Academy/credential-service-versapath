package com.capstone.repository;

import com.capstone.model.AtomSnapshot;
import com.capstone.model.CapsuleAtomMapping;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface CapsuleAtomMappingRepository extends JpaRepository<CapsuleAtomMapping, UUID> {
    // find all the atoms that belong to a growth track
    @Query("""
    SELECT cam.skillAtom
    FROM CapsuleAtomMapping cam
    WHERE cam.skillCapsule.capsuleId = :capsuleId
    """)
    List<AtomSnapshot> findAtomsByCapsuleId(@Param("capsuleId") UUID capsuleId);
}
