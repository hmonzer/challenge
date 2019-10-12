package com.revolut;

import com.revolut.challenge.domain.model.account.Account;
import com.revolut.challenge.domain.model.account.AccountId;
import com.revolut.challenge.domain.model.account.Amount;
import com.revolut.challenge.domain.services.account.InvalidAccountException;
import com.revolut.challenge.domain.services.transfer.InvalidTransferAmountException;
import com.revolut.challenge.domain.services.transfer.MoneyTransferCreationService;
import com.revolut.challenge.domain.services.transfer.MoneyTransferRequest;
import com.revolut.challenge.infra.repositories.InMemoryAccountRepository;
import io.micronaut.context.annotation.Requires;
import io.micronaut.context.env.Environment;
import io.micronaut.discovery.event.ServiceStartedEvent;
import io.micronaut.runtime.event.annotation.EventListener;
import io.micronaut.scheduling.annotation.Async;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Singleton;
import java.math.BigDecimal;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


@Singleton
@Slf4j
@Requires(notEnv = Environment.TEST) // Don't load data in tests.
public class AccountsLoader {

    private InMemoryAccountRepository accountRepository;
    private MoneyTransferCreationService moneyTransferCreationService;

    public AccountsLoader(InMemoryAccountRepository accountRepository, MoneyTransferCreationService moneyTransferCreationService) {
        this.accountRepository = accountRepository;
        this.moneyTransferCreationService = moneyTransferCreationService;
    }

    @Async
    @EventListener
    public void loadData(final ServiceStartedEvent event) throws InvalidAccountException, InvalidTransferAmountException {
        log.info("Starting to fill data for testing purposes");
        AccountId sourceAccountId = AccountId.from(UUID.fromString("08f9da92-515f-4c55-b072-235c8443f1b6"));
        AccountId beneficiaryAccountId = AccountId.from(UUID.fromString("8bcc5f7f-3fe3-49a5-9dfc-f13c0323d67e"));
        addAccount(sourceAccountId);
        addAccount(beneficiaryAccountId);
        log.info("Source and Beneficiary accounts loaded successfully");

        MoneyTransferRequest transferRequest = MoneyTransferRequest.builder().amount(Amount.builder().amount(BigDecimal.ONE).build()).beneficiaryAccountId(beneficiaryAccountId).sourceAccountId(sourceAccountId).build();
        ExecutorService executor = Executors.newFixedThreadPool(100);
        for (int i = 0; i <= 1000; i++) {
            int iterationNumber = i;
            executor.submit(() -> testMoneyTransfer(transferRequest, iterationNumber));
        }
        log.info("All money transfer requests were created");
    }

    private void testMoneyTransfer(MoneyTransferRequest transferRequest, int iterationNumber) {
        try {
            moneyTransferCreationService.requestMoneyTransfer(transferRequest);
        } catch (InvalidAccountException | InvalidTransferAmountException e) {
            log.error("Failed to transfer in attempt {}", iterationNumber);
        }
    }

    private void addAccount(AccountId sourceAccountId) {
        Account sourceAccount = Account.builder().accountId(sourceAccountId).currentBalance(Amount.builder().amount(BigDecimal.valueOf(2000)).build()).build();
        accountRepository.save(sourceAccount);
    }
}
