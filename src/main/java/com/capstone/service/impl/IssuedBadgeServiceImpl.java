package com.capstone.service.impl;

import com.capstone.exception.BadgeProcessingException;
import com.capstone.exception.CapsuleNotFoundException;
import com.capstone.exception.UserProcessingException;
import com.capstone.model.CapsuleSnapshot;
import com.capstone.model.IssuedBadge;
import com.capstone.model.Issuer;
import com.capstone.model.UserSnapshot;
import com.capstone.repository.CapsuleSnapshotRepository;
import com.capstone.repository.IssuedBadgeRepository;
import com.capstone.repository.IssuerRepository;
import com.capstone.repository.UserSnapshotRepository;
import com.capstone.service.IssuedBadgeService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.common.event.CapsuleCompletionEvent;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class IssuedBadgeServiceImpl implements IssuedBadgeService {

    private final IssuedBadgeRepository issuedBadgeRepository;
    private final UserSnapshotRepository userSnapshotRepository;
    private final CapsuleSnapshotRepository capsuleSnapshotRepository;
    private final IssuerRepository issuerRepository;

    // Cache the default issuer to avoid repeated database queries
    private Issuer cachedDefaultIssuer;

    @PostConstruct
    public void initializeDefaultIssuer() {
        try {
            cachedDefaultIssuer = issuerRepository.findAll().stream().findFirst()
                    .orElseThrow(() -> new BadgeProcessingException("No issuer found in system"));
            log.info("Cached default issuer: {} (ID: {})",
                    cachedDefaultIssuer.getName(), cachedDefaultIssuer.getIssuerId());
        } catch (Exception e) {
            log.warn("Could not initialize default issuer during startup: {}", e.getMessage());
        }
    }

    @Override
    public IssuedBadge processCapsuleCompletionEvent(CapsuleCompletionEvent event) {
        log.info("Processing capsule completion event for learnerId: {}, capsuleId: {}",
                event.getLearnerId(), event.getCapsuleId());

        try {
            // 1. Check if badge already issued
            if (isAlreadyIssued(event.getLearnerId(), event.getCapsuleId())) {
                log.info("Badge already issued for learnerId: {} and capsuleId: {}",
                        event.getLearnerId(), event.getCapsuleId());
                return findExistingBadge(event.getLearnerId(), event.getCapsuleId());
            }

            // 2. Validate existence with lightweight queries
            validateUserExists(event.getLearnerId());
            validateCapsuleExists(event.getCapsuleId());

            // 3. Get entities for foreign key references
            UserSnapshot userSnapshot = userSnapshotRepository.findByUserId(event.getLearnerId())
                    .orElseThrow(() -> new UserProcessingException("User not found with ID: " + event.getLearnerId()));

            CapsuleSnapshot capsuleSnapshot = capsuleSnapshotRepository.findByCapsuleId(event.getCapsuleId())
                    .orElseThrow(() -> new CapsuleNotFoundException("Capsule not found with ID: " + event.getCapsuleId()));

            // 4. Create badge with cached issuer
            IssuedBadge issuedBadge = IssuedBadge.builder()
                    .title(event.getCapsuleName() + " Completion Badge")
                    .description(event.getCapsuleDescription())
                    .issuedOn(LocalDate.now())
                    .verificationCode(generateVerificationCode())
                    .userSnapshot(userSnapshot)
                    .capsuleSnapshot(capsuleSnapshot)
                    .issuer(getDefaultIssuer())
                    .build();

            // 5. Save badge
            IssuedBadge savedBadge = issuedBadgeRepository.save(issuedBadge);
            log.info("Successfully issued badge with ID: {} for learnerId: {} and capsuleId: {}",
                    savedBadge.getIssuedId(), event.getLearnerId(), event.getCapsuleId());

            return savedBadge;

        } catch (DataIntegrityViolationException e) {
            throw new BadgeProcessingException("Failed to issue badge due to data constraint violation", e);
        } catch (Exception e) {
            log.error("Error processing capsule completion event for learnerId: {}, capsuleId: {}",
                    event.getLearnerId(), event.getCapsuleId(), e);
            throw new BadgeProcessingException("Failed to process capsule completion event", e);
        }
    }

    @Override
    public boolean isAlreadyIssued(UUID learnerId, UUID capsuleId) {
        return issuedBadgeRepository.existsByUserSnapshotUserIdAndCapsuleSnapshotCapsuleId(learnerId, capsuleId);
    }

    private String generateVerificationCode() {
        return UUID.randomUUID().toString().replace("-", "").substring(0, 16).toUpperCase();
    }

    private void validateUserExists(UUID userId) {
        if (!userSnapshotRepository.existsByUserId(userId)) {
            throw new UserProcessingException("User not found with ID: " + userId);
        }
    }

    private void validateCapsuleExists(UUID capsuleId) {
        if (!capsuleSnapshotRepository.existsByCapsuleId(capsuleId)) {
            throw new CapsuleNotFoundException("Capsule not found with ID: " + capsuleId);
        }
    }

    private Issuer getDefaultIssuer() {
        if (cachedDefaultIssuer == null) {
            log.warn("Default issuer cache is null, reinitializing...");
            initializeDefaultIssuer();
        }
        return cachedDefaultIssuer;
    }

    private IssuedBadge findExistingBadge(UUID learnerId, UUID capsuleId) {
        return issuedBadgeRepository.findByUserSnapshotUserIdAndCapsuleSnapshotCapsuleId(learnerId, capsuleId)
                .orElse(null);
    }

}
