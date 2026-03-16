package com.fintouch.ledger.repository;

import com.fintouch.ledger.domain.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {
}

