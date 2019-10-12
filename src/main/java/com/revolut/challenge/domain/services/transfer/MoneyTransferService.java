package com.revolut.challenge.domain.services.transfer;

import com.revolut.challenge.domain.model.account.InsufficientFundsException;
import com.revolut.challenge.domain.model.transfer.MoneyTransfer;
import com.revolut.challenge.domain.model.transfer.TransferRequestId;
import com.revolut.challenge.domain.repositories.IMoneyTransferRepository;
import com.revolut.challenge.domain.services.account.IAccountService;
import com.revolut.challenge.domain.services.account.InvalidAccountException;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;
import java.util.Optional;


@Slf4j
public class MoneyTransferService implements IMoneyTransferService {

    private IAccountService accountService;
    private IMoneyTransferRepository moneyTransferRepository;

    @Inject
    public MoneyTransferService(IAccountService accountService, IMoneyTransferRepository moneyTransferRepository) {
        this.accountService = accountService;
        this.moneyTransferRepository = moneyTransferRepository;
    }

    @Override
    public void transferMoney(TransferRequestId requestId) throws InvalidTransferRequestException {
        Optional<MoneyTransfer> moneyTransfer = moneyTransferRepository.findById(requestId);
        if (moneyTransfer.isPresent()) {
            runMoneyTransfer(moneyTransfer.get());
        } else {
            throw new InvalidTransferRequestException(requestId);
        }
    }

    private void runMoneyTransfer(MoneyTransfer moneyTransfer) {
        if (debitSourceAccount(moneyTransfer) && creditBeneficiary(moneyTransfer)) {
            markTransferComplete(moneyTransfer);
        }
    }

    private void markTransferComplete(MoneyTransfer moneyTransfer) {
        moneyTransfer.complete();
        moneyTransferRepository.save(moneyTransfer);
    }

    private boolean creditBeneficiary(MoneyTransfer moneyTransfer) {
        try {
            accountService.creditAccount(moneyTransfer.getBeneficiaryAccountId(), moneyTransfer.getAmount());
            return true;
        } catch (Exception e) {
            log.error("Failed to credit beneficiary account {}", moneyTransfer, e);
            markTransferFailedWhileCrediting(moneyTransfer);
            compensateSourceAccount(moneyTransfer);
        }
        return false;
    }

    private void compensateSourceAccount(MoneyTransfer moneyTransfer) {
        try {
            accountService.creditAccount(moneyTransfer.getSourceAccountId(), moneyTransfer.getAmount());
        } catch (Exception e) {
            log.error("Failed to compensate source account again. Manual Intervention needed {}", moneyTransfer, e);
        }
    }

    private boolean debitSourceAccount(MoneyTransfer moneyTransfer) {
        try {
            accountService.debitAccount(moneyTransfer.getSourceAccountId(), moneyTransfer.getAmount());
            return true;
        } catch (InsufficientFundsException e) {
            log.error("Failed to debit source account due to insufficient funds {}", moneyTransfer, e);
            markTransferFailedDueToInsufficientFunds(moneyTransfer);
        } catch (InvalidAccountException e) {
            log.error("Failed to debit source account {}", moneyTransfer, e);
            markTransferFailedWhileDebitingSource(moneyTransfer);
        }
        return false;
    }

    private void markTransferFailedWhileDebitingSource(MoneyTransfer moneyTransfer) {
        moneyTransfer.failWhileDebitingSource();
        moneyTransferRepository.save(moneyTransfer);
    }

    private void markTransferFailedDueToInsufficientFunds(MoneyTransfer moneyTransfer) {
        moneyTransfer.failDueToInsufficientFunds();
        moneyTransferRepository.save(moneyTransfer);
    }

    private void markTransferFailedWhileCrediting(MoneyTransfer moneyTransfer) {
        moneyTransfer.failWhileCreditingBeneficiary();
        moneyTransferRepository.save(moneyTransfer);
    }
}
