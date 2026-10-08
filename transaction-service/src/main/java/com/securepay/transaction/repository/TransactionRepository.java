package com.fraudshield.transaction.repository;

import com.fraudshield.transaction.entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface TransactionRepository
        extends JpaRepository<Transaction, UUID> {
}