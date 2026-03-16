package com.fintouch.ledger.api.dto;

import com.fintouch.ledger.domain.UserType;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateUserRequest(
        @NotBlank(message = "Nome é obrigatório")
        String fullName,
        @NotBlank(message = "Email é obrigatório")
        @Email(message = "Email inválido")
        String email,
        @NotBlank(message = "Documento é obrigatório")
        String document,
        @NotNull(message = "Tipo é obrigatório")
        UserType type
) {
}

