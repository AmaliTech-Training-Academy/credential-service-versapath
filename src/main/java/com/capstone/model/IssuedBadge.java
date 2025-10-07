package com.capstone.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.UuidGenerator;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(
        name = "issued_badges",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_learner_capsule_badge",
                        columnNames = {"user_id", "capsule_id"}
                )
        },
        indexes = {
                @Index(name = "idx_issued_badges_user_id", columnList = "user_id"),
                @Index(name = "idx_issued_badges_capsule_id", columnList = "capsule_id"),
                @Index(name = "idx_issued_badges_issuer_id", columnList = "issuer_id"),
                @Index(name = "idx_issued_badges_issued_on", columnList = "issued_on")
        }
)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IssuedBadge {

    @Id
    @UuidGenerator
    @Column(name = "issued_id", updatable = false, nullable =
            false)
    private UUID issuedId;

    @NotBlank(message = "Title is required")
    @Size(max = 255, message = "Title must not exceed 255 characters")
    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @NotNull(message = "Issued date is required")
    @Column(name = "issued_on", nullable = false)
    private LocalDate issuedOn;

    @Column(name = "expires_on")
    private LocalDate expiresOn;

    @Size(max = 64, message = "Verification code must not exceed 64 characters")
    @Column(name = "verification_code", unique = true, length
            = 64)
    private String verificationCode;

    @Column(name = "created_at", nullable = false, updatable
            = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // Relationships
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "issuer_id", nullable = false)
    @NotNull(message = "Issuer is required")
    private Issuer issuer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @NotNull(message = "User is required")
    private UserSnapshot userSnapshot;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "capsule_id", nullable = false)
    @NotNull(message = "Capsule is required")
    private CapsuleSnapshot capsuleSnapshot;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
