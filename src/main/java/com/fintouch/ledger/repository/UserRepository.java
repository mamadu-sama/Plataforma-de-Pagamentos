package com.fintouch.ledger.repository;

import com.fintouch.ledger.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
}

