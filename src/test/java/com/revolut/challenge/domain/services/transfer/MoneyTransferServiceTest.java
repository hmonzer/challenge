package com.revolut.challenge.domain.services.transfer;

import com.revolut.challenge.domain.model.account.AccountId;
import com.revolut.challenge.domain.model.account.Amount;
import com.revolut.challenge.domain.model.account.InsufficientFundsException;
import com.revolut.challenge.domain.model.transfer.MoneyTransfer;
import com.revolut.challenge.domain.model.transfer.MoneyTransferStatus;
import com.revolut.challenge.domain.model.transfer.TransferRequestId;
import com.revolut.challenge.domain.repositories.IMoneyTransferRepository;
import com.revolut.challenge.domain.services.account.AccountService;
import com.revolut.challenge.domain.services.account.InvalidAccountException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;


class MoneyTransferServiceTest {

    private MoneyTransferService moneyTransferService;
    private AccountId SOURCE_ACCOUNT_ID;
    private AccountId BENEFICIARY_ACCOUNT_ID;
    private TransferRequestId REQUEST_ID;

    private AccountService accountService;
    private IMoneyTransferRepository moneyTransferRepository;

    @BeforeEach
    public void setup() {
        SOURCE_ACCOUNT_ID = AccountId.from(UUID.randomUUID());
        BENEFICIARY_ACCOUNT_ID = AccountId.from(UUID.randomUUID());
        REQUEST_ID = TransferRequestId.from(UUID.randomUUID());
        accountService = mock(AccountService.class);
        moneyTransferRepository = new MockMoneyTransferRepository();
        moneyTransferService = new MoneyTransferService(accountService, moneyTransferRepository);
    }

    @Test
    public void transferMoney_throws_InvalidTransferRequestException_if_transfer_does_not_exist() {
        TransferRequestId transferRequestId = TransferRequestId.from(UUID.randomUUID());
        Assertions.assertThrows(InvalidTransferRequestException.class, () -> moneyTransferService.transferMoney(transferRequestId));
    }

    @Test
    public void transferMoney_debits_source_account() throws InsufficientFundsException, InvalidAccountException, InvalidTransferRequestException {
        Amount amount = Amount.builder().amount(BigDecimal.TEN).build();
        MoneyTransfer request = MoneyTransfer.builder().sourceAccountId(SOURCE_ACCOUNT_ID).beneficiaryAccountId(BENEFICIARY_ACCOUNT_ID).requestId(REQUEST_ID).amount(amount).build();
        moneyTransferRepository.save(request);
        moneyTransferService.transferMoney(request.getRequestId());
        verify(accountService).debitAccount(SOURCE_ACCOUNT_ID, amount);
    }

    @Test
    public void transferMoney_credits_beneficiary_account() throws InsufficientFundsException, InvalidAccountException, InvalidTransferRequestException {
        Amount transferAmount = Amount.builder().amount(BigDecimal.TEN).build();
        MoneyTransfer request = MoneyTransfer.builder().sourceAccountId(SOURCE_ACCOUNT_ID).beneficiaryAccountId(BENEFICIARY_ACCOUNT_ID).requestId(REQUEST_ID).amount(transferAmount).build();
        moneyTransferRepository.save(request);
        moneyTransferService.transferMoney(request.getRequestId());
        verify(accountService).creditAccount(BENEFICIARY_ACCOUNT_ID, transferAmount);
    }

    @Test
    public void transferMoney_success_updates_transferMoney_status_to_COMPLETED() throws InvalidTransferRequestException {
        Amount transferAmount = Amount.builder().amount(BigDecimal.TEN).build();
        MoneyTransfer request = MoneyTransfer.builder().sourceAccountId(SOURCE_ACCOUNT_ID).beneficiaryAccountId(BENEFICIARY_ACCOUNT_ID).requestId(REQUEST_ID).amount(transferAmount).build();
        moneyTransferRepository.save(request);
        moneyTransferService.transferMoney(request.getRequestId());
        Optional<MoneyTransfer> transfer = moneyTransferRepository.findById(request.getRequestId());
        Assertions.assertEquals(MoneyTransferStatus.COMPLETED, transfer.get().getStatus());
    }

    @Test
    public void transferMoney_does_not_credit_if_debit_source_not_completed_successfully() throws InvalidAccountException, InsufficientFundsException, InvalidTransferRequestException {
        Amount transferAmount = Amount.builder().amount(BigDecimal.TEN).build();
        MoneyTransfer request = MoneyTransfer.builder().sourceAccountId(SOURCE_ACCOUNT_ID).beneficiaryAccountId(BENEFICIARY_ACCOUNT_ID).requestId(REQUEST_ID).amount(transferAmount).build();
        moneyTransferRepository.save(request);
        doThrow(InsufficientFundsException.class).when(accountService).debitAccount(SOURCE_ACCOUNT_ID, transferAmount);
        moneyTransferService.transferMoney(request.getRequestId());
        verify(accountService, never()).creditAccount(BENEFICIARY_ACCOUNT_ID, transferAmount);
    }

    @Test
    public void transferMoney_sets_moneyTransfer_status_to_INSUFFICIENT_FUNDS_when_failed_to_debit_source() throws InvalidTransferRequestException, InvalidAccountException, InsufficientFundsException {
        Amount transferAmount = Amount.builder().amount(BigDecimal.TEN).build();
        MoneyTransfer request = MoneyTransfer.builder().sourceAccountId(SOURCE_ACCOUNT_ID).beneficiaryAccountId(BENEFICIARY_ACCOUNT_ID).requestId(REQUEST_ID).amount(transferAmount).build();
        moneyTransferRepository.save(request);
        doThrow(InsufficientFundsException.class).when(accountService).debitAccount(SOURCE_ACCOUNT_ID, transferAmount);
        moneyTransferService.transferMoney(request.getRequestId());
        Optional<MoneyTransfer> moneyTransfer = moneyTransferRepository.findById(request.getRequestId());
        Assertions.assertEquals(MoneyTransferStatus.INSUFFICIENT_FUNDS, moneyTransfer.get().getStatus());
    }

    @Test
    public void transferMoney_sets_moneyTransfer_status_to_FAILED_TO_CREDIT_BENEFICIARY_when_failed_to_credit_beneficiary() throws InvalidTransferRequestException, InvalidAccountException, InsufficientFundsException {
        Amount transferAmount = Amount.builder().amount(BigDecimal.TEN).build();
        MoneyTransfer request = MoneyTransfer.builder().sourceAccountId(SOURCE_ACCOUNT_ID).beneficiaryAccountId(BENEFICIARY_ACCOUNT_ID).requestId(REQUEST_ID).amount(transferAmount).build();
        moneyTransferRepository.save(request);
        doThrow(InvalidAccountException.class).when(accountService).creditAccount(BENEFICIARY_ACCOUNT_ID, transferAmount);
        moneyTransferService.transferMoney(request.getRequestId());
        Optional<MoneyTransfer> moneyTransfer = moneyTransferRepository.findById(request.getRequestId());
        Assertions.assertEquals(MoneyTransferStatus.FAILED_TO_CREDIT_BENEFICIARY, moneyTransfer.get().getStatus());
    }

    @Test
    public void transferMoney_compensates_source_if_failed_to_credit_beneficiary() throws InvalidTransferRequestException, InvalidAccountException, InsufficientFundsException {
        Amount transferAmount = Amount.builder().amount(BigDecimal.TEN).build();
        MoneyTransfer request = MoneyTransfer.builder().sourceAccountId(SOURCE_ACCOUNT_ID).beneficiaryAccountId(BENEFICIARY_ACCOUNT_ID).requestId(REQUEST_ID).amount(transferAmount).build();
        moneyTransferRepository.save(request);
        doThrow(InvalidAccountException.class).when(accountService).creditAccount(BENEFICIARY_ACCOUNT_ID, transferAmount);
        moneyTransferService.transferMoney(request.getRequestId());
        verify(accountService).creditAccount(SOURCE_ACCOUNT_ID, transferAmount);
    }
}