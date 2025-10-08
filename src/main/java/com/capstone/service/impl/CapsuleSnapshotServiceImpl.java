package com.capstone.service.impl;

import com.capstone.exception.AtomNotFoundException;
import com.capstone.exception.CapsuleAtomMappingException;
import com.capstone.exception.CapsuleProcessingException;
import com.capstone.exception.DuplicateCapsuleException;
import com.capstone.mapper.CapsuleEventMapper;
import com.capstone.model.AtomSnapshot;
import com.capstone.model.CapsuleAtomMapping;
import com.capstone.model.CapsuleSnapshot;
import com.capstone.repository.AtomSnapshotRepository;
import com.capstone.repository.CapsuleSnapshotRepository;
import com.capstone.service.CapsuleSnapshotService;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.common.event.SkillCapsuleEvent;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class CapsuleSnapshotServiceImpl implements CapsuleSnapshotService {

    private final CapsuleSnapshotRepository capsuleSnapshotRepository;
    private final CapsuleEventMapper capsuleEventMapper;
    private final AtomSnapshotRepository atomSnapshotRepository;

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

            if (event.getSkillAtom() != null && !event.getSkillAtom().isEmpty()) {
                smartUpdateCapsuleAtomMappings(savedCapsule, event.getSkillAtom());
                log.info("Successfully created atom mappings for capsule {}", savedCapsule.getCapsuleId());
            }

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

            if (event.getSkillAtom() != null && !event.getSkillAtom().isEmpty()) {
                smartUpdateCapsuleAtomMappings(updatedCapsule, event.getSkillAtom());
                log.info("Successfully updated atom mappings for capsule {}", updatedCapsule.getCapsuleId());
            }

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

    @Override
    public void smartUpdateCapsuleAtomMappings(CapsuleSnapshot capsule, List<Map<UUID, Integer>> skillAtomMappings) {
        log.debug("Smart updating atom mappings for capsule: {}", capsule.getCapsuleId());

        try {
            // PHASE 1: VERIFY ALL ATOMS EXIST - Fail Fast Strategy
            List<AtomSequencePair> newMappings = verifyAndParseAtoms(skillAtomMappings);
            log.debug("Verified {} atom mappings for capsule {}", newMappings.size(), capsule.getCapsuleId());

            // PHASE 2: ANALYZE CHANGES - Smart Diff Algorithm
            UpdateAnalysis analysis = analyzeChanges(capsule, newMappings);
            log.debug("Analysis for capsule {}: {} to add, {} to update",
                    capsule.getCapsuleId(), analysis.getToAdd().size(), analysis.getToUpdate().size());

            // PHASE 3: APPLY UPDATES - Atomic Operation
            applyAtomMappingUpdates(capsule, analysis);

            log.info("Smart update completed for capsule {}: {} added, {} updated, {} preserved",
                    capsule.getCapsuleId(), analysis.getToAdd().size(),
                    analysis.getToUpdate().size(), analysis.getPreserved());

        } catch (AtomNotFoundException e) {
            log.error("Atom verification failed for capsule {}: {}", capsule.getCapsuleId(), e.getMessage());
            throw new CapsuleAtomMappingException("Atom not found: " + e.getMessage());
        } catch (Exception e) {
            log.error("Smart update failed for capsule {}: {}", capsule.getCapsuleId(), e.getMessage(), e);
            throw new CapsuleAtomMappingException("Smart update failed", e);
        }
    }

    /**
     * Verify all atoms exist and parse into structured format
     */
    private List<AtomSequencePair> verifyAndParseAtoms(List<Map<UUID, Integer>> skillAtomMappings) {
        List<AtomSequencePair> parsedMappings = new ArrayList<>();

        for (Map<UUID, Integer> atomMap : skillAtomMappings) {
            for (Map.Entry<UUID, Integer> entry : atomMap.entrySet()) {
                UUID atomId = entry.getKey();
                Integer sequence = entry.getValue();

                // Verify atom exists
                AtomSnapshot atom = atomSnapshotRepository.findByAtomId(atomId)
                        .orElseThrow(() -> new AtomNotFoundException("Atom not found with ID: " + atomId));

                // Validate sequence order
                if (sequence == null || sequence < 1) {
                    throw new CapsuleAtomMappingException("Invalid sequence order: " + sequence);
                }

                parsedMappings.add(new AtomSequencePair(atom, sequence));
            }
        }

        return parsedMappings;
    }

    /**
     * Analyze differences between existing and new mappings
     */
    private UpdateAnalysis analyzeChanges(CapsuleSnapshot capsule, List<AtomSequencePair> newMappings) {
        // Build lookup map of existing mappings - O(n)
        Map<UUID, CapsuleAtomMapping> existingMap = capsule.getCapsuleAtomMappings()
                .stream()
                .collect(Collectors.toMap(
                        mapping -> mapping.getSkillAtom().getAtomId(),
                        mapping -> mapping
                ));

        List<CapsuleAtomMapping> toAdd = new ArrayList<>();
        List<CapsuleAtomMapping> toUpdate = new ArrayList<>();
        int preserved = 0;

        // Process each new mapping - O(m)
        for (AtomSequencePair newMapping : newMappings) {
            UUID atomId = newMapping.getAtom().getAtomId();

            if (existingMap.containsKey(atomId)) {
                // ATOM EXISTS - Check if sequence changed
                CapsuleAtomMapping existing = existingMap.get(atomId);
                if (!existing.getSequenceOrder().equals(newMapping.getSequence())) {
                    existing.setSequenceOrder(newMapping.getSequence());
                    toUpdate.add(existing);
                } else {
                    preserved++; // No change needed
                }
                // Remove from map to track what remains
                existingMap.remove(atomId);
            } else {
                // NEW ATOM - Create mapping
                CapsuleAtomMapping newMappingEntity = CapsuleAtomMapping.builder()
                        .skillCapsule(capsule)
                        .skillAtom(newMapping.getAtom())
                        .sequenceOrder(newMapping.getSequence())
                        .build();
                toAdd.add(newMappingEntity);
            }
        }

        // Remaining mappings in existingMap are preserved (not in new event)
        preserved += existingMap.size();

        return new UpdateAnalysis(toAdd, toUpdate, preserved);
    }

    /**
     * Apply the analyzed updates to the capsule
     */
    private void applyAtomMappingUpdates(CapsuleSnapshot capsule, UpdateAnalysis analysis) {
        if (!analysis.getToAdd().isEmpty()) {
            capsule.getCapsuleAtomMappings().addAll(analysis.getToAdd());
        }
        // Note: toUpdate mappings are already modified by reference in analyzeChanges
    }

    // INNER CLASSES
    @Data
    @AllArgsConstructor
    private static class AtomSequencePair {
        private AtomSnapshot atom;
        private Integer sequence;
    }

    @Data
    @AllArgsConstructor
    private static class UpdateAnalysis {
        private List<CapsuleAtomMapping> toAdd;
        private List<CapsuleAtomMapping> toUpdate;
        private int preserved;
    }
}
