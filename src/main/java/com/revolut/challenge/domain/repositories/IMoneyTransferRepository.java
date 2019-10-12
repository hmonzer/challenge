package com.revolut.challenge.domain.repositories;

import com.revolut.challenge.domain.model.transfer.MoneyTransfer;
import com.revolut.challenge.domain.model.transfer.TransferRequestId;

import java.util.Optional;


public interface IMoneyTransferRepository {
    void save(MoneyTransfer account);

    Optional<MoneyTransfer> findById(TransferRequestId accountId);
}
