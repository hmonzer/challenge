package com.revolut.challenge.domain.model.account;

import com.revolut.challenge.domain.model.account.Account;
import com.revolut.challenge.domain.model.account.AccountId;
import com.revolut.challenge.domain.model.account.Amount;
import com.revolut.challenge.domain.model.account.InsufficientFundsException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;


class AccountTest {

    @Test
    public void currentBalance_for_newly_created_account_is_zero() {
        Account account = Account.builder().accountId(AccountId.from(UUID.randomUUID())).build();
        Amount expectedAmount = Amount.builder().amount(BigDecimal.ZERO).build();
        assertEquals(expectedAmount, account.getCurrentBalance());
    }

    @Test
    public void credit_account_adds_to_account_current_balance() {
        Account account = Account.builder().accountId(AccountId.from(UUID.randomUUID())).build();
        creditAccount(account, BigDecimal.TEN);
        creditAccount(account, BigDecimal.valueOf(5));

        Amount expectedAmount = Amount.builder().amount(BigDecimal.valueOf(15)).build();

        Assertions.assertEquals(expectedAmount, account.getCurrentBalance());
    }

    @Test
    public void debit_account_without_credit_throws_exception() {
        Account account = Account.builder().accountId(AccountId.from(UUID.randomUUID())).build();
        assertThrows(InsufficientFundsException.class, () -> account.debit(Amount.builder().amount(BigDecimal.TEN).build()));
    }

    @Test
    public void debit_account_throws_insufficient_funds_if_requested_amount_greater_than_balance() throws InsufficientFundsException {
        Account account = Account.builder().accountId(AccountId.from(UUID.randomUUID())).build();
        creditAccount(account, BigDecimal.ONE);
        Amount amountToDebit = Amount.builder().amount(BigDecimal.TEN).build();
        Assertions.assertThrows(InsufficientFundsException.class, () -> account.debit(amountToDebit));
    }

    @Test
    public void debit_account_reduces_current_balance_if_funds_current_amaount_is_Enough() throws InsufficientFundsException {
        Account account = Account.builder().accountId(AccountId.from(UUID.randomUUID())).build();
        creditAccount(account, BigDecimal.TEN);
        Amount amountToDebit = Amount.builder().amount(BigDecimal.ONE).build();
        account.debit(amountToDebit);
        Amount expectedAmount = Amount.builder().amount(BigDecimal.valueOf(9)).build();
        Assertions.assertEquals(expectedAmount, account.getCurrentBalance());
    }

    @Test
    public void debit_account_successful_if_request_amount_equals_current_balance() throws InsufficientFundsException {
        Account account = Account.builder().accountId(AccountId.from(UUID.randomUUID())).build();
        creditAccount(account, BigDecimal.TEN);
        Amount amountToDebit = Amount.builder().amount(BigDecimal.TEN).build();
        account.debit(amountToDebit);
        Amount expectedAmount = Amount.builder().amount(BigDecimal.ZERO).build();
        Assertions.assertEquals(expectedAmount, account.getCurrentBalance());
    }

    private void creditAccount(Account account, BigDecimal ten) {
        Amount amountToCredit = Amount.builder().amount(ten).build();
        account.credit(amountToCredit);
    }
}