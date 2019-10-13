package com.revolut.challenge.infra.repositories;

import com.revolut.challenge.domain.model.account.Account;
import com.revolut.challenge.domain.model.account.AccountId;
import com.revolut.challenge.domain.model.account.Amount;
import com.revolut.challenge.domain.model.account.InsufficientFundsException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.ConcurrentModificationException;
import java.util.Optional;
import java.util.UUID;


class InMemoryAccountRepositoryTest {

    private InMemoryAccountRepository repository;

    @BeforeEach
    void setup() {
        repository = new InMemoryAccountRepository();
    }

    @Test
    void test_find_and_save()  {
        AccountId accountId = AccountId.from(UUID.randomUUID());
        Account account = Account.builder().accountId(accountId).build();
        repository.save(account);
        Optional<Account> savedAccount = repository.findById(accountId);
        Assertions.assertNotNull(savedAccount.get());
        Assertions.assertEquals(account, savedAccount.get());
    }

    @Test
    void test_find_returns_a_copy_of_the_account() {
        AccountId accountId = AccountId.from(UUID.randomUUID());
        Account account = Account.builder().accountId(accountId).build();
        repository.save(account);
        Optional<Account> savedAccount = repository.findById(accountId);
        savedAccount.get().credit(Amount.builder().amount(BigDecimal.TEN).build());
        Assertions.assertNotEquals(savedAccount.get().getCurrentBalance(), account.getCurrentBalance());
    }

    @Test
    void test_save_saves_a_copy_of_the_account() {
        AccountId accountId = AccountId.from(UUID.randomUUID());
        Account account = Account.builder().accountId(accountId).build();
        repository.save(account);
        account.credit(Amount.builder().amount(BigDecimal.TEN).build());

        Optional<Account> savedAccount = repository.findById(accountId);
        Assertions.assertNotEquals(savedAccount.get().getCurrentBalance(), account.getCurrentBalance());
    }

    @Test
    void save_instance_increases_its_version() {
        AccountId accountId = AccountId.from(UUID.randomUUID());
        Account account = Account.builder().accountId(accountId).build();
        Assertions.assertEquals(0, account.getVersion());
        repository.save(account);
        Optional<Account> savedAccount = repository.findById(accountId);
        Assertions.assertEquals(1, savedAccount.get().getVersion());
    }

    @Test
    void save_instance_does_not_increase_if_no_changes_were_made_on_instance() {
        AccountId accountId = AccountId.from(UUID.randomUUID());
        Account account = Account.builder().accountId(accountId).build();
        repository.save(account);
        Optional<Account> accountToSaveWithoutChanging = repository.findById(accountId);
        repository.save(accountToSaveWithoutChanging.get()); //save it once without changing anything; other than the first initialization
        Optional<Account> savedAccount = repository.findById(accountId);
        Assertions.assertEquals(1, savedAccount.get().getVersion());
    }

    @Test
    void save_instance_with_non_matching_version_throws_ConcurrencyException() throws InsufficientFundsException {
        AccountId accountId = AccountId.from(UUID.randomUUID());
        Account account = Account.builder().accountId(accountId).currentBalance(Amount.builder().amount(BigDecimal.TEN).build()).build();
        repository.save(account);
        Optional<Account> firstLoadedAccount = repository.findById(accountId);
        firstLoadedAccount.get().debit(Amount.builder().amount(BigDecimal.ONE).build());

        Optional<Account> secondLoadedAccount = repository.findById(accountId);
        secondLoadedAccount.get().debit(Amount.builder().amount(BigDecimal.ONE).build());

        repository.save(firstLoadedAccount.get());

        Assertions.assertThrows(ConcurrentModificationException.class, () -> repository.save(secondLoadedAccount.get()));
    }
}