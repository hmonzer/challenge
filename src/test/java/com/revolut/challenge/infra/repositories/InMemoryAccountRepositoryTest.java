package com.revolut.challenge.infra.repositories;

import com.revolut.challenge.domain.model.account.Account;
import com.revolut.challenge.domain.model.account.AccountId;
import com.revolut.challenge.domain.model.account.Amount;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;


class InMemoryAccountRepositoryTest {

    private InMemoryAccountRepository repository;

    @BeforeEach
    public void setup() {
        repository = new InMemoryAccountRepository();
    }

    @Test
    public void test_find_and_save()  {
        AccountId accountId = AccountId.from(UUID.randomUUID());
        Account account = Account.builder().accountId(accountId).build();
        repository.save(account);
        Optional<Account> savedAccount = repository.findById(accountId);
        Assertions.assertNotNull(savedAccount.get());
        Assertions.assertEquals(account, savedAccount.get());
    }

    @Test
    public void test_find_returns_a_copy_of_the_account() {
        AccountId accountId = AccountId.from(UUID.randomUUID());
        Account account = Account.builder().accountId(accountId).build();
        repository.save(account);
        Optional<Account> savedAccount = repository.findById(accountId);
        savedAccount.get().credit(Amount.builder().amount(BigDecimal.TEN).build());
        Assertions.assertNotEquals(savedAccount.get().getCurrentBalance(), account.getCurrentBalance());
    }

    @Test
    public void test_save_saves_a_copy_of_the_account() {
        AccountId accountId = AccountId.from(UUID.randomUUID());
        Account account = Account.builder().accountId(accountId).build();
        repository.save(account);
        account.credit(Amount.builder().amount(BigDecimal.TEN).build());

        Optional<Account> savedAccount = repository.findById(accountId);
        Assertions.assertNotEquals(savedAccount.get().getCurrentBalance(), account.getCurrentBalance());
    }

}