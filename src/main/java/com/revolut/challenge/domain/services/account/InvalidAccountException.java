package com.revolut.challenge.domain.services.account;

import com.revolut.challenge.domain.model.account.AccountId;


public class InvalidAccountException extends Exception {
    public InvalidAccountException(AccountId accountId) {
        super(String.format("Invalid Account Specified %s", accountId.toString()));
    }
}
