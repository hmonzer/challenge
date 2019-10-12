package com.revolut.challenge.domain.services.transfer;

import io.micronaut.runtime.event.annotation.EventListener;
import io.micronaut.scheduling.annotation.Async;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Singleton;


@Singleton
@Slf4j
public class MoneyTransferProcessingService {

    private IMoneyTransferService moneyTransferService;

    public MoneyTransferProcessingService(IMoneyTransferService moneyTransferService) {
        this.moneyTransferService = moneyTransferService;
    }

    @Async
    @EventListener
    public void onMoneyTransferCreated(MoneyTransferCreatedEvent event) throws Throwable {
        log.info("Processing money transfer created: {}", event);
        moneyTransferService.transferMoney(event.getRequestId());
    }
}
