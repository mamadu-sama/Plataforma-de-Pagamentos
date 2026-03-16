package com.fintouch.ledger.infra.api;

import com.fintouch.ledger.infra.exception.ApiException;
import com.fintouch.ledger.infra.exception.ExternalDependencyException;
import com.fintouch.ledger.infra.exception.NotFoundException;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.List;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleValidation(MethodArgumentNotValidException ex) {
        List<String> details = ex.getBindingResult().getFieldErrors().stream()
                .map(this::formatFieldError)
                .toList();

        return ResponseEntity.badRequest().body(new ApiErrorResponse(
                "VALIDATION_ERROR",
                "Pedido inválido",
                details,
                Instant.now()
        ));
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiErrorResponse> handleConstraintViolation(ConstraintViolationException ex) {
        List<String> details = ex.getConstraintViolations().stream()
                .map(v -> v.getPropertyPath() + ": " + v.getMessage())
                .toList();

        return ResponseEntity.badRequest().body(new ApiErrorResponse(
                "VALIDATION_ERROR",
                "Pedido inválido",
                details,
                Instant.now()
        ));
    }

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleNotFound(NotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ApiErrorResponse(
                ex.getCode(),
                ex.getMessage(),
                List.of(),
                Instant.now()
        ));
    }

    @ExceptionHandler(ExternalDependencyException.class)
    public ResponseEntity<ApiErrorResponse> handleExternal(ExternalDependencyException ex) {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(new ApiErrorResponse(
                ex.getCode(),
                ex.getMessage(),
                List.of(),
                Instant.now()
        ));
    }

    @ExceptionHandler(ApiException.class)
    public ResponseEntity<ApiErrorResponse> handleBusiness(ApiException ex) {
        return ResponseEntity.unprocessableEntity().body(new ApiErrorResponse(
                ex.getCode(),
                ex.getMessage(),
                List.of(),
                Instant.now()
        ));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleUnknown(Exception ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ApiErrorResponse(
                "INTERNAL_ERROR",
                "Erro inesperado",
                List.of(),
                Instant.now()
        ));
    }

    private String formatFieldError(FieldError fe) {
        String field = fe.getField();
        String msg = fe.getDefaultMessage();
        Object rejected = fe.getRejectedValue();
        if (rejected == null) {
            return field + ": " + msg;
        }
        return field + ": " + msg + " (valor=" + rejected + ")";
    }
}

