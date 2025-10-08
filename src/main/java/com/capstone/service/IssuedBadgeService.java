package com.capstone.service;

import com.capstone.model.IssuedBadge;
import org.common.event.CapsuleCompletionEvent;

import java.util.UUID;

public interface IssuedBadgeService {
    IssuedBadge processCapsuleCompletionEvent(CapsuleCompletionEvent event);
    boolean isAlreadyIssued(UUID learnerId, UUID capsuleId);
}
