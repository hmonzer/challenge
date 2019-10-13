package com.revolut.challenge.domain.model.account;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.math.BigDecimal;


@EqualsAndHashCode
@Getter
@Builder(toBuilder = true)
public class Account {
    private AccountId accountId;
    @Builder.Default
    @EqualsAndHashCode.Exclude
    private int version = 0;

    @EqualsAndHashCode.Exclude
    private boolean modified;

    @Builder.Default
    private Amount currentBalance = Amount.builder().amount(BigDecimal.ZERO).build();

    public void debit(Amount amountToDebit) throws InsufficientFundsException {
        if (currentBalance.isLessThan(amountToDebit)) {
            throw new InsufficientFundsException(amountToDebit.getAmount());
        }
        currentBalance = currentBalance.subtract(amountToDebit);
        modified = true;
    }

    public void credit(Amount amountToCredit) {
        currentBalance = currentBalance.add(amountToCredit);
        modified = true;
    }
}
