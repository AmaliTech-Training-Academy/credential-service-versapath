package com.capstone.service;

import com.capstone.dto.response.CapsuleResponseDto;
import com.capstone.dto.response.PaginatedResponseDto;
import com.capstone.model.CapsuleSnapshot;
import org.common.event.SkillCapsuleEvent;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public interface CapsuleSnapshotService {
    CapsuleSnapshot processCapsuleEvent(SkillCapsuleEvent event);
    CapsuleSnapshot createCapsule(SkillCapsuleEvent event);
    CapsuleSnapshot updateCapsule(CapsuleSnapshot existingCapsule, SkillCapsuleEvent event);
    Optional<CapsuleSnapshot> findByCapsuleId(UUID skillCapsuleId);
    void smartUpdateCapsuleAtomMappings(CapsuleSnapshot capsule, List<Map<UUID, Integer>> skillAtomMappings);
    CapsuleSnapshot assignAtomsToCapsule(SkillCapsuleEvent event);
    Optional<CapsuleResponseDto> findByCapsuleIdWithAtomSummaries(UUID skillCapsuleId);
    PaginatedResponseDto<CapsuleResponseDto> findAllBasic(Pageable pageable);
    PaginatedResponseDto<CapsuleResponseDto> findAllWithAtomSummaries(Pageable pageable);

}
