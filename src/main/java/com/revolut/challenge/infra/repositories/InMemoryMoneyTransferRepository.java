package com.revolut.challenge.infra.repositories;

import com.revolut.challenge.domain.model.transfer.MoneyTransfer;
import com.revolut.challenge.domain.model.transfer.TransferRequestId;
import com.revolut.challenge.domain.repositories.IMoneyTransferRepository;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Singleton;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.locks.StampedLock;

@Slf4j
@Singleton
public class InMemoryMoneyTransferRepository implements IMoneyTransferRepository {

    private Map<TransferRequestId, MoneyTransfer> moneyTransfers;
    private StampedLock lock;

    public InMemoryMoneyTransferRepository() {
        moneyTransfers = Collections.synchronizedMap(new HashMap<>());
        lock = new StampedLock();
    }

    @Override
    public void save(MoneyTransfer moneyTransfer) {
        long writeLockStamp = lock.writeLock();
        try {
            moneyTransfers.put(moneyTransfer.getRequestId(), moneyTransfer.toBuilder().build());
        } finally {
            lock.unlock(writeLockStamp);
        }
    }

    @Override
    public Optional<MoneyTransfer> findById(TransferRequestId transferRequestId) {
        long readLockStamp = lock.readLock();
        try {
            MoneyTransfer moneyTransfer = moneyTransfers.get(transferRequestId);
            return moneyTransfers.containsKey(transferRequestId) ? Optional.of(moneyTransfer.toBuilder().build()) : Optional.empty();
        } finally {
            lock.unlock(readLockStamp);
        }
    }
}
