package com.revolut.challenge.domain.model.account;

import lombok.Value;

import java.util.UUID;

@Value
public class AccountId {
    private UUID id;

    public static AccountId from(UUID uuid) {
        return new AccountId(uuid);
    }
}
