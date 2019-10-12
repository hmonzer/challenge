package com.revolut.challenge.domain.services.transfer;

import com.revolut.challenge.domain.model.transfer.TransferRequestId;


public interface IMoneyTransferService {
    void transferMoney(TransferRequestId requestId) throws InvalidTransferRequestException;
}
