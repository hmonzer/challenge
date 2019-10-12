package com.revolut.challenge.domain.services.account;

import com.revolut.challenge.domain.model.account.AccountId;
import com.revolut.challenge.domain.model.account.Amount;
import com.revolut.challenge.domain.model.account.InsufficientFundsException;


public interface IAccountService {
    boolean isAccountValid(AccountId accountId);
    void creditAccount(AccountId accountId, Amount amount) throws InvalidAccountException;
    void debitAccount(AccountId accountId, Amount amount) throws InvalidAccountException, InsufficientFundsException;

    AccountId createAccount(Amount initialAmount);
}
