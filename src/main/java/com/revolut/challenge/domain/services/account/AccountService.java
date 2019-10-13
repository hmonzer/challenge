package com.revolut.challenge.domain.services.account;

import com.revolut.challenge.domain.model.account.Account;
import com.revolut.challenge.domain.model.account.AccountId;
import com.revolut.challenge.domain.model.account.Amount;
import com.revolut.challenge.domain.model.account.InsufficientFundsException;
import com.revolut.challenge.domain.repositories.IAccountRepository;
import io.micronaut.retry.annotation.Retryable;

import javax.inject.Inject;
import java.util.Optional;
import java.util.UUID;


public class AccountService implements IAccountService {

    private IAccountRepository accountRepository;

    @Inject
    public AccountService(IAccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    @Retryable(delay = "200ms")
    @Override
    public void creditAccount(AccountId accountId, Amount amount) throws InvalidAccountException {
        Optional<Account> account = this.accountRepository.findById(accountId);
        Account accountToCredit = account.orElseThrow(() -> new InvalidAccountException(accountId));
        accountToCredit.credit(amount);
        accountRepository.save(accountToCredit);
    }

    @Retryable
    @Override
    public void debitAccount(AccountId accountId, Amount amount) throws InvalidAccountException, InsufficientFundsException {
        Optional<Account> account = this.accountRepository.findById(accountId);
        Account accountToDebit = account.orElseThrow(() -> new InvalidAccountException(accountId));
        accountToDebit.debit(amount);
        accountRepository.save(accountToDebit);
    }

    @Override
    public AccountId createAccount(Amount initialAmount) {
        Account account = Account.builder().accountId(AccountId.from(UUID.randomUUID())).currentBalance(initialAmount).build();
        accountRepository.save(account);
        return account.getAccountId();
    }

    @Override
    public boolean isAccountValid(AccountId accountId) {
        Optional<Account> account = accountRepository.findById(accountId);
        return account.isPresent();
    }
}
