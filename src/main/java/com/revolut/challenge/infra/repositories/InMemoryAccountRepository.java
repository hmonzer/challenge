package com.revolut.challenge.infra.repositories;

import com.revolut.challenge.domain.model.account.Account;
import com.revolut.challenge.domain.model.account.AccountId;
import com.revolut.challenge.domain.repositories.IAccountRepository;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Singleton;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.locks.StampedLock;


@Slf4j
@Singleton
public class InMemoryAccountRepository implements IAccountRepository {

    private Map<AccountId, Account> accounts;
    private StampedLock lock;

    public InMemoryAccountRepository() {
        accounts = Collections.synchronizedMap(new HashMap<>());
        lock = new StampedLock();
    }

    @Override
    public void save(Account account) {
        long writeLockStamp = lock.writeLock();
        try {
            accounts.put(account.getAccountId(), account.toBuilder().build());
        } finally {
            lock.unlock(writeLockStamp);
        }
    }

    @Override
    public Optional<Account> findById(AccountId accountId) {
        long readLockStamp = lock.readLock();
        try {
            Account account = accounts.get(accountId);
            return accounts.containsKey(accountId) ? Optional.of(account.toBuilder().build()) : Optional.empty();
        } finally {
            lock.unlock(readLockStamp);
        }
    }
}
