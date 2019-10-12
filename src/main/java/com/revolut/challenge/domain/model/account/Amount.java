package com.revolut.challenge.domain.model.account;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Builder;
import lombok.Value;

import java.math.BigDecimal;


@Value
@Builder
public class Amount {
    private BigDecimal amount;

    public Amount add(Amount amountToAdd) {
        return Amount.builder().amount(amount.add(amountToAdd.getAmount())).build();
    }

    public Amount subtract(Amount amountToSubtract) {
        return Amount.builder().amount(amount.subtract(amountToSubtract.getAmount())).build();
    }

    public boolean isLessThan(Amount amountToCompare) {
        return amount.compareTo(amountToCompare.getAmount()) < 0;
    }

    @JsonIgnore
    public boolean isNegative() {
        return amount.signum() < 0;
    }
}
