package com.capstone.model;

import jakarta.persistence.*;
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
@Table(name = "issued_badges")
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
    @JoinColumn(name = "badge_id", nullable = false)
    @NotNull(message = "Badge is required")
    private Badge badge;

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
