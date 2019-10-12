package com.revolut.challenge.domain.services.transfer;

import com.revolut.challenge.domain.model.transfer.TransferRequestId;
import com.revolut.challenge.domain.services.account.InvalidAccountException;


public interface IMoneyTransferCreationService {
    TransferRequestId requestMoneyTransfer(MoneyTransferRequest request) throws InvalidAccountException, InvalidTransferAmountException;
}
