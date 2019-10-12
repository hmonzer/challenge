package com.revolut.challenge.domain.model.account;

import java.math.BigDecimal;


public class InsufficientFundsException extends Exception {

    public InsufficientFundsException(BigDecimal amount) {
        super("Insufficient Funds to debit " + amount.toPlainString());
    }
}