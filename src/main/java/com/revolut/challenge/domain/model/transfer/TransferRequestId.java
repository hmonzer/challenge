package com.revolut.challenge.domain.model.transfer;

import lombok.Builder;
import lombok.Value;

import java.util.UUID;

@Builder
@Value
public class TransferRequestId {
    private UUID id;

    public static TransferRequestId from(UUID uuid) {
        return TransferRequestId.builder().id(uuid).build();
    }
}
