package com.capstone.controller;

import com.capstone.dto.response.ApiResponseDto;
import com.capstone.dto.response.LearnerBadgeResponseDto;
import com.capstone.dto.response.PaginatedResponseDto;
import com.capstone.service.IssuedBadgeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Pattern;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/badges")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Badge Management", description = "API for managing and retrieving issued badges")
public class IssuedBadgeController {

    private final IssuedBadgeService issuedBadgeService;

    @GetMapping("/learner/{learnerId}")
    @Operation(
            summary = "Get badges for a specific learner",
            description = "Retrieve all badges issued to a specific learner with pagination support. Instructors and Admins can view any learner's badges."
            )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Badges retrieved successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request parameters"),
            @ApiResponse(responseCode = "403", description = "Access denied - insufficient permissions"),
            @ApiResponse(responseCode = "404", description = "Learner not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PreAuthorize("hasAnyRole('INSTRUCTOR', 'ADMIN','LEARNER')")
    public ResponseEntity<ApiResponseDto<PaginatedResponseDto<LearnerBadgeResponseDto>>> getLearnerBadges(
            @Parameter(description = "Learner ID", required = true)
            @PathVariable UUID learnerId,

            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        log.info("Request to get badges for learnerId: {} (page: {}, size: {})",
                learnerId, page, size);

        // Limit page size to prevent abuse
        size = Math.min(size, 100);

        Pageable pageable = PageRequest.of(page, size,
                Sort.by(Sort.Direction.DESC, "issuedOn").and(Sort.by(Sort.Direction.DESC, "createdAt")));

        PaginatedResponseDto<LearnerBadgeResponseDto> badges = issuedBadgeService.getLearnerBadges(learnerId, pageable);

        log.info("Retrieved {} badges for learnerId: {}", badges.getItems().size(), learnerId);

        return ResponseEntity.ok(
                ApiResponseDto.success(badges, "Badges retrieved successfully")
        );
    }

    @GetMapping("/my-badges")
    @Operation(
            summary = "Get current learner's badges",
            description = "Retrieve all badges issued to the currently authenticated learner"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Badges retrieved successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request parameters"),
            @ApiResponse(responseCode = "401", description = "Authentication required"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<ApiResponseDto<PaginatedResponseDto<LearnerBadgeResponseDto>>> getMyBadges(
            @Parameter(description = "Current user ID from header", hidden = true)
            @RequestHeader("X-User-Id") UUID currentUserId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        log.info("Request to get own badges by learner: {} (page: {}, size: {})",
                currentUserId, page, size);

        // Limit page size to prevent abuse
        size = Math.min(size, 100);

        Pageable pageable = PageRequest.of(page, size,
                Sort.by(Sort.Direction.DESC, "issuedOn").and(Sort.by(Sort.Direction.DESC, "createdAt")));

        PaginatedResponseDto<LearnerBadgeResponseDto> badges = issuedBadgeService.getLearnerBadges(currentUserId, pageable);

        log.info("Retrieved {} badges for current learner: {}", badges.getItems().size(), currentUserId);

        return ResponseEntity.ok(
                ApiResponseDto.success(badges, "Your badges retrieved successfully")
        );
    }

    @GetMapping("/verify/{verificationCode}")
    @Operation(
            summary = "Verify a badge by verification code",
            description = "Verify and retrieve badge details using verification code. This endpoint is public for verification purposes."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Badge verified successfully"),
            @ApiResponse(responseCode = "404", description = "Badge not found with the provided verification code"),
            @ApiResponse(responseCode = "400", description = "Invalid verification code format"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<ApiResponseDto<LearnerBadgeResponseDto>> verifyBadge(
            @Parameter(description = "Badge verification code", required = true, example = "F3E5428D9C014D91")
            @PathVariable
            @Pattern(regexp = "^[A-Z0-9]{16}$", message = "Verification code must be 16 alphanumeric characters")
            String verificationCode) {

        log.info("Request to verify badge with code: {}", verificationCode);

        Optional<LearnerBadgeResponseDto> badge = issuedBadgeService.getBadgeByVerificationCode(verificationCode);

        if (badge.isPresent()) {
            log.info("Badge verified successfully for code: {}, badgeId: {}",
                    verificationCode, badge.get().getBadgeId());

            return ResponseEntity.ok(
                    ApiResponseDto.success(badge.get(), "Badge verified successfully")
            );
        } else {
            log.warn("Badge verification failed - no badge found with code: {}", verificationCode);

            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponseDto.error("Badge not found with the provided verification code", "Badge verification failed"));
        }
    }

    @GetMapping("/learner/{learnerId}/count")
    @Operation(
            summary = "Get badge count for a learner",
            description = "Get the total number of badges issued to a specific learner"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Badge count retrieved successfully"),
            @ApiResponse(responseCode = "403", description = "Access denied - insufficient permissions"),
            @ApiResponse(responseCode = "404", description = "Learner not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PreAuthorize("hasAnyRole('INSTRUCTOR', 'ADMIN')")
    public ResponseEntity<ApiResponseDto<Long>> getLearnerBadgeCount(
            @Parameter(description = "Learner ID", required = true)
            @PathVariable UUID learnerId) {

        log.info("Request to get badge count for learnerId: {}",
                learnerId);

        long badgeCount = issuedBadgeService.countLearnerBadges(learnerId);

        log.info("Badge count for learnerId {}: {}", learnerId, badgeCount);

        return ResponseEntity.ok(
                ApiResponseDto.success(badgeCount, "Badge count retrieved successfully")
        );
    }

    @GetMapping("/my-badges/count")
    @Operation(
            summary = "Get current learner's badge count",
            description = "Get the total number of badges issued to the currently authenticated learner"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Badge count retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Authentication required"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<ApiResponseDto<Long>> getMyBadgeCount(
            @Parameter(description = "Current user ID from header", hidden = true)
            @RequestHeader("X-User-Id") UUID currentUserId) {

        log.info("Request to get own badge count by learner: {}", currentUserId);

        long badgeCount = issuedBadgeService.countLearnerBadges(currentUserId);

        log.info("Badge count for current learner {}: {}", currentUserId, badgeCount);

        return ResponseEntity.ok(
                ApiResponseDto.success(badgeCount, "Your badge count retrieved successfully")
        );
    }
}
