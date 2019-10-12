package com.revolut.challenge.domain.services.transfer;

import com.revolut.challenge.domain.model.transfer.TransferRequestId;
import lombok.Builder;
import lombok.Value;


@Value
@Builder
public class MoneyTransferCreatedEvent {
    private TransferRequestId requestId;
}
