package com.capstone.repository;

import com.capstone.model.Issuer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface IssuerRepository extends JpaRepository<Issuer, UUID> {
}
