package com.fintouch.ledger.api.dto;

import com.fintouch.ledger.domain.UserType;

public record CreateUserResponse(
        Long id,
        String fullName,
        String email,
        String document,
        UserType type
) {
}

