package com.revolut.challenge.domain.services.transfer;

import com.revolut.challenge.domain.model.account.AccountId;
import com.revolut.challenge.domain.model.account.Amount;
import lombok.Builder;
import lombok.Data;


@Builder
@Data
public class MoneyTransferRequest {
    private AccountId beneficiaryAccountId;
    private AccountId sourceAccountId;
    private Amount amount;
}
