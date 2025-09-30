package com.capstone.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "capsule_snapshot")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = {"issuedBadges"})
public class CapsuleSnapshot {

    @Id
    @UuidGenerator
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @NotNull(message = "Skill capsule ID is required")
    @Column(nullable = false, unique = true, updatable = false, name = "capsule_id")
    private UUID capsuleId;

    @NotBlank(message = "Capsule name is required")
    @Size(max = 255, message = "Capsule name must not exceed 255 characters")
    @Column(name = "capsule_name", nullable = false)
    private String capsuleName;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "difficulty_level", length = 20)
    private String difficultyLevel;

    @Size(max = 50, message = "Proficiency level must not exceed 50 characters")
    @Column(name = "proficiency_level", length = 50)
    private String proficiencyLevel;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "capsuleSnapshot", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<IssuedBadge> issuedBadges;

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
