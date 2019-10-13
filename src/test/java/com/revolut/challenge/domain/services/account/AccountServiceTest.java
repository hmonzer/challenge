package com.revolut.challenge.domain.services.account;

import com.revolut.challenge.domain.model.account.Account;
import com.revolut.challenge.domain.model.account.AccountId;
import com.revolut.challenge.domain.model.account.Amount;
import com.revolut.challenge.domain.model.account.InsufficientFundsException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.Mockito.mock;


class AccountServiceTest {

    private AccountService accountService;
    private MockAccountRepository accountRepository;
    private AccountId ACCOUNT_ID;
    private Account ACCOUNT;
    private IAccountLockingService accountLockingService;

    @BeforeEach
    void setup() {
        ACCOUNT_ID = AccountId.from(UUID.randomUUID());
        ACCOUNT = Account.builder().accountId(ACCOUNT_ID).build();
        accountLockingService = mock(IAccountLockingService.class);
        accountRepository = new MockAccountRepository();
        accountRepository.save(ACCOUNT);
        accountService = new AccountService(accountRepository, accountLockingService);
    }

    @Test
    void creditAccount_credits_account_said_amount() throws InvalidAccountException {
        Amount amount = Amount.builder().amount(BigDecimal.TEN).build();
        accountService.creditAccount(ACCOUNT_ID, amount);
        Optional<Account> resultAccount = accountRepository.findById(ACCOUNT_ID);
        Assertions.assertEquals(amount, resultAccount.get().getCurrentBalance());
    }

    @Test
    void debitAccount_debits_account_from_said_amount() throws InvalidAccountException, InsufficientFundsException {
        Amount amount = Amount.builder().amount(BigDecimal.TEN).build();
        accountService.creditAccount(ACCOUNT_ID, amount);
        accountService.debitAccount(ACCOUNT_ID, Amount.builder().amount(BigDecimal.valueOf(6)).build());

        Optional<Account> resultAccount = accountRepository.findById(ACCOUNT_ID);
        Amount expectedAmount = Amount.builder().amount(BigDecimal.valueOf(4)).build();
        Assertions.assertEquals(expectedAmount, resultAccount.get().getCurrentBalance());
    }

    @Test
    void creditAccount_throws_InvalidAccountException_if_account_invalid() {
        Assertions.assertThrows(InvalidAccountException.class, () -> accountService.creditAccount(AccountId.from(UUID.randomUUID()), Amount.builder().amount(BigDecimal.TEN).build()));
    }

    @Test
    void debitAccount_throws_InvalidAccountException_if_account_invalid() {
        Assertions.assertThrows(InvalidAccountException.class, () -> accountService.debitAccount(AccountId.from(UUID.randomUUID()), Amount.builder().amount(BigDecimal.TEN).build()));
    }

    @Test
    void debitAccount_throws_InsufficientFunds_exception_if_account_cannot_be_debited() {
        Amount amount = Amount.builder().amount(BigDecimal.TEN).build();
        Assertions.assertThrows(InsufficientFundsException.class, () -> accountService.debitAccount(ACCOUNT_ID, amount));
    }

    @Test
    void isAccountValid_returns_true_if_account_exists() {
        Assertions.assertTrue(accountService.isAccountValid(ACCOUNT_ID));
    }

    @Test
    void isAccountValid_returns_false_it_account_not_existent() {
        Assertions.assertFalse(accountService.isAccountValid(AccountId.from(UUID.randomUUID())));
    }

    @Test
    void createAccount_inserts_a_new_account() {
        Amount initialAmount = Amount.builder().amount(BigDecimal.TEN).build();
        AccountId accountId = accountService.createAccount(initialAmount);
        Optional<Account> account = accountRepository.findById(accountId);
        Account expectedAccount = Account.builder().accountId(accountId).currentBalance(initialAmount).build();
        Assertions.assertEquals(expectedAccount, account.get());
    }
}