package com.revolut.challenge.domain.services.account;

import com.revolut.challenge.domain.model.account.AccountId;


public interface IAccountLockingService {
    void lockAccount(AccountId accountId);

    void unlockAccount(AccountId accountId);
}
