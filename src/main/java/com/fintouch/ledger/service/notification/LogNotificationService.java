package com.fintouch.ledger.service.notification;

import com.fintouch.ledger.domain.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(prefix = "fintouch.notification", name = "mode", havingValue = "log", matchIfMissing = true)
public class LogNotificationService implements NotificationService {

    private static final Logger log = LoggerFactory.getLogger(LogNotificationService.class);

    @Override
    public void notify(Transaction transaction) {
        log.info("Notificação enviada para transacção {}", transaction.getId());
    }
}

