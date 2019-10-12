package com.revolut.challenge.domain.model.account;

import com.revolut.challenge.domain.model.account.Amount;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;


class AmountTest {

    @Test
    public void add_amount() {
        Amount originalAmount = Amount.builder().amount(BigDecimal.TEN).build();
        Amount amountToAdd = Amount.builder().amount(BigDecimal.ONE).build();

        Amount expectedAmount = Amount.builder().amount(BigDecimal.valueOf(11)).build();
        Assertions.assertEquals(expectedAmount, originalAmount.add(amountToAdd));
    }

    @Test
    public void subtract_amount() {
        Amount originalAmount = Amount.builder().amount(BigDecimal.TEN).build();
        Amount amountToSubtract = Amount.builder().amount(BigDecimal.ONE).build();

        Amount expectedAmount = Amount.builder().amount(BigDecimal.valueOf(9)).build();
        Assertions.assertEquals(expectedAmount, originalAmount.subtract(amountToSubtract));
    }

    @Test
    public void isLessThan_returns_false_if_amount_is_bigger() {
        Amount originalAmount = Amount.builder().amount(BigDecimal.TEN).build();
        Amount otherAmount = Amount.builder().amount(BigDecimal.ONE).build();

        Assertions.assertFalse(originalAmount.isLessThan(otherAmount));
    }

    @Test
    public void isLessThan_returns_true_if_amount_is_smaller() {
        Amount originalAmount = Amount.builder().amount(BigDecimal.ONE).build();
        Amount otherAmount = Amount.builder().amount(BigDecimal.TEN).build();

        Assertions.assertTrue(originalAmount.isLessThan(otherAmount));
    }

    @Test
    public void isLessThan_returns_true_if_amounts_are_equal() {
        Amount originalAmount = Amount.builder().amount(BigDecimal.TEN).build();
        Amount otherAmount = Amount.builder().amount(BigDecimal.TEN).build();

        Assertions.assertFalse(originalAmount.isLessThan(otherAmount));
    }

    @Test
    public void isNegative_returns_true_if_amount_is_negative() {
        Amount negativeAmount = Amount.builder().amount(BigDecimal.valueOf(-4.5)).build();
        Assertions.assertTrue(negativeAmount.isNegative());
    }

    @Test
    public void isNegative_returns_false_if_amount_is_positive() {
        Amount negativeAmount = Amount.builder().amount(BigDecimal.valueOf(4.5)).build();
        Assertions.assertFalse(negativeAmount.isNegative());
    }
}