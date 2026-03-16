package com.fintouch.ledger.service.notification;

import com.fintouch.ledger.domain.Transaction;

public interface NotificationService {

    void notify(Transaction transaction);
}

