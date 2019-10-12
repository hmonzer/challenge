package com.revolut.challenge.domain.services.transfer;

import com.revolut.challenge.domain.model.account.Amount;


public class InvalidTransferAmountException extends Exception {

    public InvalidTransferAmountException(Amount amount) {
        super(String.format("Invalid Transfer Amount: %s", amount.getAmount().toPlainString()));
    }
}
