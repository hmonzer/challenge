package com.revolut.challenge.domain.services.transfer;

import com.revolut.challenge.domain.model.account.AccountId;
import com.revolut.challenge.domain.model.transfer.MoneyTransfer;
import com.revolut.challenge.domain.model.transfer.TransferRequestId;
import com.revolut.challenge.domain.repositories.IMoneyTransferRepository;
import com.revolut.challenge.domain.services.account.IAccountService;
import com.revolut.challenge.domain.services.account.InvalidAccountException;
import io.micronaut.context.event.ApplicationEventPublisher;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;
import java.util.UUID;


@Slf4j
public class MoneyTransferCreationService implements IMoneyTransferCreationService {

    private IMoneyTransferRepository moneyTransferRepository;
    private IAccountService accountService;
    private ApplicationEventPublisher publisher;

    @Inject
    public MoneyTransferCreationService(IMoneyTransferRepository moneyTransferRepository, IAccountService accountService, ApplicationEventPublisher publisher) {
        this.moneyTransferRepository = moneyTransferRepository;
        this.accountService = accountService;
        this.publisher = publisher;
    }

    @Override
    public TransferRequestId requestMoneyTransfer(MoneyTransferRequest request) throws InvalidAccountException, InvalidTransferAmountException {
        throwIfRequestInvalid(request);
        TransferRequestId transferRequestId = createMoneyTransfer(request);
        publishMoneyTransferCreatedEvent(transferRequestId);
        return transferRequestId;
    }

    private void publishMoneyTransferCreatedEvent(TransferRequestId transferRequestId) {
        publisher.publishEvent(MoneyTransferCreatedEvent.builder().requestId(transferRequestId).build());
    }

    private TransferRequestId createMoneyTransfer(MoneyTransferRequest request) {
        TransferRequestId transferRequestId = TransferRequestId.from(UUID.randomUUID());
        MoneyTransfer moneyTransfer = MoneyTransfer.builder().requestId(transferRequestId).amount(request.getAmount()).beneficiaryAccountId(request.getBeneficiaryAccountId()).sourceAccountId(request.getSourceAccountId())
                                                   .build();
        moneyTransferRepository.save(moneyTransfer);
        return transferRequestId;
    }

    private void throwIfRequestInvalid(MoneyTransferRequest request) throws InvalidAccountException, InvalidTransferAmountException {
        throwIfAccountInvalid(request.getSourceAccountId());
        throwIfAccountInvalid(request.getBeneficiaryAccountId());
        throwIfAmountNegative(request);
    }

    private void throwIfAmountNegative(MoneyTransferRequest request) throws InvalidTransferAmountException {
        if (request.getAmount().isNegative()) {
            throw new InvalidTransferAmountException(request.getAmount());
        }
    }

    private void throwIfAccountInvalid(AccountId sourceAccountId) throws InvalidAccountException {
        if (!accountService.isAccountValid(sourceAccountId)) {
            throw new InvalidAccountException(sourceAccountId);
        }
    }
}
