package com.revolut.challenge.domain.services.transfer;

import com.revolut.challenge.domain.model.transfer.TransferRequestId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;


class MoneyTransferProcessingServiceTest {

    private MoneyTransferProcessingService moneyTransferProcessingService;
    private IMoneyTransferService moneyTransferService;
    private TransferRequestId TRANSFER_REQUEST_ID;
    private MoneyTransferCreatedEvent MONEY_TRANSFER_CREATED_EVENT;

    @BeforeEach
    public void setup() {
        TRANSFER_REQUEST_ID = TransferRequestId.from(UUID.randomUUID());
        MONEY_TRANSFER_CREATED_EVENT = MoneyTransferCreatedEvent.builder().requestId(TRANSFER_REQUEST_ID).build();
        moneyTransferService = mock(IMoneyTransferService.class);
        moneyTransferProcessingService = new MoneyTransferProcessingService(moneyTransferService);
    }

    @Test
    public void onMoneyTransferCreated_triggers_transfer_request() throws Throwable {
        moneyTransferProcessingService.onMoneyTransferCreated(MONEY_TRANSFER_CREATED_EVENT);
        verify(moneyTransferService).transferMoney(TRANSFER_REQUEST_ID);
    }
}