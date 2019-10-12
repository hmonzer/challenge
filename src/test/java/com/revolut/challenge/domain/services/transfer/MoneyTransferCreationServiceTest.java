package com.revolut.challenge.domain.services.transfer;

import com.revolut.challenge.domain.model.account.AccountId;
import com.revolut.challenge.domain.model.account.Amount;
import com.revolut.challenge.domain.model.transfer.MoneyTransfer;
import com.revolut.challenge.domain.model.transfer.MoneyTransferStatus;
import com.revolut.challenge.domain.model.transfer.TransferRequestId;
import com.revolut.challenge.domain.services.account.IAccountService;
import com.revolut.challenge.domain.services.account.InvalidAccountException;
import io.micronaut.context.event.ApplicationEventPublisher;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


class MoneyTransferCreationServiceTest {

    private MoneyTransferCreationService moneyTransferCreationService;
    private AccountId SOURCE_ACCOUNT_ID;
    private AccountId BENEFICIARY_ACCOUNT_ID;
    private MockMoneyTransferRepository repository;
    private IAccountService accountService;
    private ApplicationEventPublisher publisher;

    @BeforeEach
    public void setup() {
        SOURCE_ACCOUNT_ID = AccountId.from(UUID.randomUUID());
        BENEFICIARY_ACCOUNT_ID = AccountId.from(UUID.randomUUID());
        repository = new MockMoneyTransferRepository();
        publisher = mock(ApplicationEventPublisher.class);
        accountService = mock(IAccountService.class);
        when(accountService.isAccountValid(SOURCE_ACCOUNT_ID)).thenReturn(true);
        when(accountService.isAccountValid(BENEFICIARY_ACCOUNT_ID)).thenReturn(true);
        moneyTransferCreationService = new MoneyTransferCreationService(repository, accountService, publisher);
    }

    @Test
    public void requestMoneyTransfer_creates_a_moneyTransfer() throws InvalidAccountException, InvalidTransferAmountException {
        Amount amount = Amount.builder().amount(BigDecimal.TEN).build();
        MoneyTransferRequest request = MoneyTransferRequest.builder().beneficiaryAccountId(BENEFICIARY_ACCOUNT_ID).sourceAccountId(SOURCE_ACCOUNT_ID).amount(amount).build();
        TransferRequestId transferRequestId = moneyTransferCreationService.requestMoneyTransfer(request);
        Optional<MoneyTransfer> moneyTransfer = repository.findById(transferRequestId);
        Assertions.assertTrue(moneyTransfer.isPresent());
    }

    @Test
    public void requestMoneyTransfer_creates_a_moneyTransfer_with_correct_parameters() throws InvalidAccountException, InvalidTransferAmountException {
        Amount amount = Amount.builder().amount(BigDecimal.TEN).build();
        MoneyTransferRequest request = MoneyTransferRequest.builder().beneficiaryAccountId(BENEFICIARY_ACCOUNT_ID).sourceAccountId(SOURCE_ACCOUNT_ID).amount(amount).build();
        TransferRequestId transferRequestId = moneyTransferCreationService.requestMoneyTransfer(request);
        Optional<MoneyTransfer> moneyTransfer = repository.findById(transferRequestId);

        MoneyTransfer expected = MoneyTransfer.builder().requestId(transferRequestId).amount(amount).beneficiaryAccountId(BENEFICIARY_ACCOUNT_ID).sourceAccountId(SOURCE_ACCOUNT_ID).status(MoneyTransferStatus.CREATED).build();
        Assertions.assertEquals(expected, moneyTransfer.get());
    }

    @Test
    public void requestMoneyTransfer_throws_exception_if_source_account_invalid() {
        Amount amount = Amount.builder().amount(BigDecimal.TEN).build();
        MoneyTransferRequest request = MoneyTransferRequest.builder().beneficiaryAccountId(BENEFICIARY_ACCOUNT_ID).sourceAccountId(AccountId.from(UUID.randomUUID())).amount(amount).build();
        Assertions.assertThrows(InvalidAccountException.class, () -> moneyTransferCreationService.requestMoneyTransfer(request));
    }

    @Test
    public void requestMoneyTransfer_throws_exception_if_beneficiary_account_invalid() {
        Amount amount = Amount.builder().amount(BigDecimal.TEN).build();
        MoneyTransferRequest request = MoneyTransferRequest.builder().beneficiaryAccountId(AccountId.from(UUID.randomUUID())).sourceAccountId(SOURCE_ACCOUNT_ID).amount(amount).build();
        Assertions.assertThrows(InvalidAccountException.class, () -> moneyTransferCreationService.requestMoneyTransfer(request));
    }

    @Test
    public void requestMoneyTransfer_throws_InvalidAmountException_if_amount_is_negative() {
        Amount invalidAmount = Amount.builder().amount(BigDecimal.valueOf(-10)).build();
        MoneyTransferRequest request = MoneyTransferRequest.builder().beneficiaryAccountId(BENEFICIARY_ACCOUNT_ID).sourceAccountId(SOURCE_ACCOUNT_ID).amount(invalidAmount).build();
        Assertions.assertThrows(InvalidTransferAmountException.class, () -> moneyTransferCreationService.requestMoneyTransfer(request));
    }
}