package com.fraudshield.auth.repository;

import com.fraudshield.auth.entity.AuthSession;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface AuthSessionRepository extends JpaRepository<AuthSession, UUID> {
    Optional<AuthSession> findByTransactionId(UUID transactionId);
}