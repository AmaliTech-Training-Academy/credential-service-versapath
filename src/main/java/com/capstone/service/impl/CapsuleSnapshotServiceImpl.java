package com.capstone.service.impl;

import com.capstone.exception.CapsuleProcessingException;
import com.capstone.exception.DuplicateCapsuleException;
import com.capstone.mapper.CapsuleEventMapper;
import com.capstone.model.CapsuleSnapshot;
import com.capstone.repository.CapsuleSnapshotRepository;
import com.capstone.service.CapsuleSnapshotService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.common.event.SkillCapsuleEvent;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class CapsuleSnapshotServiceImpl implements CapsuleSnapshotService {

    private final CapsuleSnapshotRepository capsuleSnapshotRepository;
    private final CapsuleEventMapper capsuleEventMapper;

    @Override
    public CapsuleSnapshot processCapsuleEvent(SkillCapsuleEvent event) {
        log.info("Processing skill capsule event for capsuleId: {}", event.getId());

        try {
            Optional<CapsuleSnapshot> existingCapsule = capsuleSnapshotRepository.findByCapsuleId(event.getId());

            if (existingCapsule.isPresent()) {
                log.info("Capsule exists, updating capsule with ID: {}", event.getId());
                return updateCapsule(existingCapsule.get(), event);
            } else {
                log.info("Capsule does not exist, creating new capsule with ID: {}", event.getId());
                return createCapsule(event);
            }

        } catch (Exception e) {
            log.error("Error processing skill capsule event for capsuleId: {}", event.getId(), e);
            throw new CapsuleProcessingException("Failed to process skill capsule event", e);
        }
    }

    @Override
    public CapsuleSnapshot createCapsule(SkillCapsuleEvent event) {
        log.debug("Creating new skill capsule from event: {}", event);

        try {

            CapsuleSnapshot newCapsule = capsuleEventMapper.toCapsuleSnapshot(event);
            CapsuleSnapshot savedCapsule = capsuleSnapshotRepository.save(newCapsule);
            log.info("Successfully created skill capsule with ID: {}", savedCapsule.getCapsuleId());


            return savedCapsule;

        } catch (DataIntegrityViolationException e) {
            log.error("Data integrity violation when creating capsule with ID: {}", event.getId(), e);
            throw new DuplicateCapsuleException(event.getId());
        } catch (Exception e) {
            log.error("Unexpected error creating capsule with ID: {}", event.getId(), e);
            throw new CapsuleProcessingException("Failed to create skill capsule", e);
        }
    }

    @Override
    public CapsuleSnapshot updateCapsule(CapsuleSnapshot existingCapsule, SkillCapsuleEvent event) {
        log.debug("Updating existing capsule {} with event data", existingCapsule.getCapsuleId());

        try {

            capsuleEventMapper.updateCapsuleSnapshot(event, existingCapsule);
            CapsuleSnapshot updatedCapsule = capsuleSnapshotRepository.save(existingCapsule);

            log.info("Successfully updated skill capsule with ID: {}", updatedCapsule.getCapsuleId());
            return updatedCapsule;

        } catch (DataIntegrityViolationException e) {
            log.error("Data integrity violation when updating capsule with ID: {}", existingCapsule.getCapsuleId(), e);
            throw new CapsuleProcessingException("Capsule update failed due to data constraint violation", e);
        } catch (Exception e) {
            log.error("Unexpected error updating capsule with ID: {}", existingCapsule.getCapsuleId(), e);
            throw new CapsuleProcessingException("Failed to update skill capsule", e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<CapsuleSnapshot> findByCapsuleId(UUID capsuleId) {
        log.debug("Finding capsule by CapsuleId: {}", capsuleId);
        return capsuleSnapshotRepository.findByCapsuleId(capsuleId);
    }
}
