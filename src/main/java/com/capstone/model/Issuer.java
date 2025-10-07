package com.capstone.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "issuers")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = {"issuedBadges"})
public class Issuer {

    @Id
    @UuidGenerator
    @Column(name = "issuer_id", updatable = false, nullable =
            false)
    private UUID issuerId;

    @NotBlank(message = "Name is required")
    @Size(max = 255, message = "Name must not exceed 255 characters")
    @Column(nullable = false)
    private String name;

    @Size(max = 255, message = "Website URL must not exceed 255 characters")
    @Column(name = "website_url")
    private String websiteUrl;

    @Email(message = "Contact email should be valid")
    @Size(max = 255, message = "Contact email must not exceed 255 characters")
    @Column(name = "contact_email")
    private String contactEmail;

    @Column(name = "created_at", nullable = false, updatable
            = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // Relationships
    @OneToMany(mappedBy = "issuer", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
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
