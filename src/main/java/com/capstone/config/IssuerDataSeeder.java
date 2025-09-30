package com.capstone.config;

import com.capstone.model.Issuer;
import com.capstone.repository.IssuerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class IssuerDataSeeder implements CommandLineRunner {

    private final IssuerRepository issuerRepository;

    @Value("${ISSUER_NAME:VersaPath}")
    private String issuerName;

    @Value("${ISSUER_WEBSITE:https://app.versapath.net/}")
    private String issuerWebsiteUrl;

    @Value("${ISSUER_CONTACT_EMAIL:credentials@versapath.com}")
    private String issuerContactEmail;

    @Override
    public void run(String... args) throws Exception {
            seedIssuerData();
    }

    private void seedIssuerData() {
        log.info("Starting Issuer data seeding...");

        // Check if any issuers already exist
        if (issuerRepository.count() > 0) {
            log.info("Issuer data already exists. Skipping seeding.");
            return;
        }

        // Create the platform issuer
        Issuer platformIssuer = Issuer.builder()
                .name(issuerName)
                .websiteUrl(issuerWebsiteUrl)
                .contactEmail(issuerContactEmail)
                .build();

        try {
            Issuer savedIssuer = issuerRepository.save(platformIssuer);
            log.info("Successfully seeded Issuer data: {} with ID: {}",
                    savedIssuer.getName(), savedIssuer.getIssuerId());
        } catch (Exception e) {
            log.error("Error seeding Issuer data: {}", e.getMessage(), e);
        }

        log.info("Issuer data seeding completed.");
    }
}
