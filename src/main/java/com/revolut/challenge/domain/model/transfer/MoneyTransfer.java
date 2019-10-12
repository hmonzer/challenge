package com.revolut.challenge.domain.model.transfer;

import com.revolut.challenge.domain.model.account.AccountId;
import com.revolut.challenge.domain.model.account.Amount;
import lombok.Builder;
import lombok.Data;

@Builder(toBuilder = true)
@Data
public class MoneyTransfer {
    private TransferRequestId requestId;
    private AccountId beneficiaryAccountId;
    private AccountId sourceAccountId;
    private Amount amount;
    @Builder.Default
    private MoneyTransferStatus status = MoneyTransferStatus.CREATED;

    public void complete() {
        this.status = MoneyTransferStatus.COMPLETED;
    }

    public void failDueToInsufficientFunds() {
        this.status = MoneyTransferStatus.INSUFFICIENT_FUNDS;
    }

    public void failWhileDebitingSource() {
        this.status = MoneyTransferStatus.FAILED_TO_DEBIT_SOURCE;
    }

    public void failWhileCreditingBeneficiary() {
        this.status = MoneyTransferStatus.FAILED_TO_CREDIT_BENEFICIARY;
    }
}
