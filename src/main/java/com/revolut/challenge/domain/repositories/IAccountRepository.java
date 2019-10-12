package com.revolut.challenge.domain.repositories;

import com.revolut.challenge.domain.model.account.Account;
import com.revolut.challenge.domain.model.account.AccountId;

import java.util.Optional;


public interface IAccountRepository {
    void save(Account account);

    Optional<Account> findById(AccountId accountId);
}
