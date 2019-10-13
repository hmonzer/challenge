package com.revolut.challenge.domain.services.account;

import com.google.common.util.concurrent.Striped;
import com.revolut.challenge.domain.model.account.AccountId;

import javax.inject.Singleton;
import java.util.concurrent.locks.Lock;


@Singleton
public class AccountLockingService implements IAccountLockingService {

    private final Striped<Lock> lockStripes;

    public AccountLockingService() {
        lockStripes = Striped.lock(100);
    }

    @Override
    public void lockAccount(AccountId accountId) {
        Lock lock = this.lockStripes.get(accountId);
        lock.lock();
    }

    @Override
    public void unlockAccount(AccountId accountId) {
        Lock lock = this.lockStripes.get(accountId);
        lock.unlock();
    }
}
