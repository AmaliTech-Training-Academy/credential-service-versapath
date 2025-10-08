package com.capstone.service;

import com.capstone.dto.response.LearnerBadgeResponseDto;
import com.capstone.dto.response.PaginatedResponseDto;
import com.capstone.model.IssuedBadge;
import org.common.event.CapsuleCompletionEvent;
import org.springframework.data.domain.Pageable;

import java.util.Optional;
import java.util.UUID;

public interface IssuedBadgeService {
    IssuedBadge processCapsuleCompletionEvent(CapsuleCompletionEvent event);
    boolean isAlreadyIssued(UUID learnerId, UUID capsuleId);

    PaginatedResponseDto<LearnerBadgeResponseDto> getLearnerBadges(UUID learnerId, Pageable pageable);
    Optional<LearnerBadgeResponseDto> getBadgeByVerificationCode(String verificationCode);
    long countLearnerBadges(UUID learnerId);
}
