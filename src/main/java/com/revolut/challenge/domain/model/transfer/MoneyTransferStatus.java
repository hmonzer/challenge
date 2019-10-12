package com.revolut.challenge.domain.model.transfer;

public enum MoneyTransferStatus {
    CREATED, COMPLETED, INSUFFICIENT_FUNDS, FAILED_TO_CREDIT_BENEFICIARY, FAILED_TO_DEBIT_SOURCE
}
