package com.capstone.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LearnerBadgeResponseDto {
    private UUID badgeId;
    private String title;
    private String description;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate issuedOn;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate expiresOn;

    private String verificationCode;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;

    // Issuer information
    private String issuerName;
    private String issuerWebsite;

    // Learner information
    private UUID learnerId;
    private String learnerFirstName;
    private String learnerLastName;
    private String learnerEmail;

    private List<AtomNameDto> atoms;
}
