package com.revolut.challenge.domain.services.account;

import com.revolut.challenge.domain.model.account.Account;
import com.revolut.challenge.domain.model.account.AccountId;
import com.revolut.challenge.domain.model.account.Amount;
import com.revolut.challenge.domain.model.account.InsufficientFundsException;
import com.revolut.challenge.domain.repositories.IAccountRepository;

import javax.inject.Inject;
import java.util.Optional;
import java.util.UUID;


public class AccountService implements IAccountService {

    private IAccountRepository accountRepository;
    private IAccountLockingService accountLockingService;

    @Inject
    public AccountService(IAccountRepository accountRepository, IAccountLockingService accountLockingService) {
        this.accountRepository = accountRepository;
        this.accountLockingService = accountLockingService;
    }

    @Override
    public void creditAccount(AccountId accountId, Amount amount) throws InvalidAccountException {
        try {
            accountLockingService.lockAccount(accountId);
            Optional<Account> account = this.accountRepository.findById(accountId);
            Account accountToCredit = account.orElseThrow(() -> new InvalidAccountException(accountId));
            accountToCredit.credit(amount);
            accountRepository.save(accountToCredit);
        } finally {
            accountLockingService.unlockAccount(accountId);
        }
    }

    @Override
    public void debitAccount(AccountId accountId, Amount amount) throws InvalidAccountException, InsufficientFundsException {
        try {
            accountLockingService.lockAccount(accountId);
            Optional<Account> account = this.accountRepository.findById(accountId);
            Account accountToDebit = account.orElseThrow(() -> new InvalidAccountException(accountId));
            accountToDebit.debit(amount);
            accountRepository.save(accountToDebit);
        } finally {
            accountLockingService.unlockAccount(accountId);
        }
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
