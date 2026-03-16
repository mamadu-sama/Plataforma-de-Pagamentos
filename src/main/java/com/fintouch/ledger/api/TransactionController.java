package com.fintouch.ledger.api;

import com.fintouch.ledger.api.dto.TransferRequest;
import com.fintouch.ledger.api.dto.TransferResponse;
import com.fintouch.ledger.domain.Transaction;
import com.fintouch.ledger.service.TransactionService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/transaction")
public class TransactionController {

    private final TransactionService transactionService;

    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TransferResponse transfer(@Valid @RequestBody TransferRequest request) {
        Transaction tx = transactionService.transfer(request.value(), request.payer(), request.payee());
        return new TransferResponse(tx.getId(), tx.getValue(), tx.getPayer().getId(), tx.getPayee().getId(), tx.getCreatedAt());
    }
}

