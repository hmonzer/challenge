package com.revolut.challenge.domain.services.account;

import com.revolut.challenge.domain.model.account.Account;
import com.revolut.challenge.domain.model.account.AccountId;
import com.revolut.challenge.domain.model.account.Amount;
import com.revolut.challenge.domain.model.account.InsufficientFundsException;
import com.revolut.challenge.domain.repositories.IAccountRepository;

import javax.inject.Inject;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.locks.StampedLock;


public class AccountService implements IAccountService {

    private IAccountRepository accountRepository;
    private StampedLock lock;

    @Inject
    public AccountService(IAccountRepository accountRepository) {
        this.accountRepository = accountRepository;
        lock = new StampedLock();
    }

    @Override
    public void creditAccount(AccountId accountId, Amount amount) throws InvalidAccountException {
        long lockStamp = lock.writeLock();
        try {
            Optional<Account> account = this.accountRepository.findById(accountId);
            Account accountToCredit = account.orElseThrow(() -> new InvalidAccountException(accountId));
            accountToCredit.credit(amount);
            accountRepository.save(accountToCredit);
        } finally {
            lock.unlock(lockStamp);
        }
    }

    @Override
    public void debitAccount(AccountId accountId, Amount amount) throws InvalidAccountException, InsufficientFundsException {
        long lockStamp = lock.writeLock();
        try {
            Optional<Account> account = this.accountRepository.findById(accountId);
            Account accountToDebit = account.orElseThrow(() -> new InvalidAccountException(accountId));
            accountToDebit.debit(amount);
            accountRepository.save(accountToDebit);
        } finally {
            lock.unlock(lockStamp);
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
