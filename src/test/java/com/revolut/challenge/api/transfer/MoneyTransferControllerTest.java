package com.revolut.challenge.api.transfer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.revolut.challenge.domain.model.account.AccountId;
import com.revolut.challenge.domain.model.account.Amount;
import com.revolut.challenge.domain.model.transfer.MoneyTransfer;
import com.revolut.challenge.domain.model.transfer.MoneyTransferStatus;
import com.revolut.challenge.domain.model.transfer.TransferRequestId;
import com.revolut.challenge.domain.repositories.IMoneyTransferRepository;
import com.revolut.challenge.domain.services.account.InvalidAccountException;
import com.revolut.challenge.domain.services.transfer.IMoneyTransferCreationService;
import com.revolut.challenge.domain.services.transfer.InvalidTransferAmountException;
import com.revolut.challenge.domain.services.transfer.MoneyTransferCreationService;
import com.revolut.challenge.domain.services.transfer.MoneyTransferRequest;
import com.revolut.challenge.infra.repositories.InMemoryMoneyTransferRepository;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.MutableHttpRequest;
import io.micronaut.http.client.RxHttpClient;
import io.micronaut.http.client.annotation.Client;
import io.micronaut.http.client.exceptions.HttpClientResponseException;
import io.micronaut.test.annotation.MicronautTest;
import io.micronaut.test.annotation.MockBean;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.inject.Inject;
import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


@MicronautTest
class MoneyTransferControllerTest {
    @Inject
    IMoneyTransferCreationService moneyTransferCreationService;
    @Inject
    IMoneyTransferRepository moneyTransferRepository;

    @Inject
    @Client("/")
    private RxHttpClient client;

    @BeforeEach
    public void setup() throws InvalidAccountException, InvalidTransferAmountException {
        TransferRequestId createdTransferId = TransferRequestId.from(UUID.randomUUID());
        when(moneyTransferCreationService.requestMoneyTransfer(any())).thenReturn(createdTransferId);
    }

    @Test
    public void getMoneyTransfer_returns_ok_when_found() {
        TransferRequestId transferId = TransferRequestId.from(UUID.randomUUID());
        MoneyTransfer moneyTransfer = MoneyTransfer.builder().requestId(transferId).sourceAccountId(AccountId.from(UUID.randomUUID())).beneficiaryAccountId(AccountId.from(UUID.randomUUID()))
                                                   .amount(Amount.builder().amount(BigDecimal.TEN).build()).build();
        when(moneyTransferRepository.findById(transferId)).thenReturn(Optional.of(moneyTransfer));
        MutableHttpRequest<Object> apiRequest = HttpRequest.GET("/transfers/" + transferId.getId());
        HttpResponse<Object> response = client.toBlocking().exchange(apiRequest);
        Assertions.assertEquals(HttpStatus.OK, response.getStatus());
    }

    @Test
    public void getMoneyTransfer_returns_moneyTransfer_in_body() throws JsonProcessingException {
        TransferRequestId transferId = TransferRequestId.from(UUID.randomUUID());
        AccountId sourceAccountId = AccountId.from(UUID.randomUUID());
        AccountId beneficiaryAccountId = AccountId.from(UUID.randomUUID());
        MoneyTransfer moneyTransfer = MoneyTransfer.builder().requestId(transferId).sourceAccountId(sourceAccountId).beneficiaryAccountId(beneficiaryAccountId).amount(Amount.builder().amount(BigDecimal.TEN).build()).build();
        when(moneyTransferRepository.findById(transferId)).thenReturn(Optional.of(moneyTransfer));
        MutableHttpRequest<Object> apiRequest = HttpRequest.GET("/transfers/" + transferId.getId());
        String response = client.toBlocking().retrieve(apiRequest);
        MoneyTransferDTO moneyTransferDTO = MoneyTransferDTO.builder().status(MoneyTransferStatus.CREATED).amount(10).beneficiaryAccountId(beneficiaryAccountId.getId()).sourceAccountId(sourceAccountId.getId()).build();
        Assertions.assertEquals(new ObjectMapper().writeValueAsString(moneyTransferDTO), response);
    }

    @Test
    public void getMoneyTransfer_returns_not_found_if_invalid_transferId() throws JsonProcessingException {
        MutableHttpRequest<Object> apiRequest = HttpRequest.GET("/transfers/" + UUID.randomUUID());
        try {
            client.toBlocking().exchange(apiRequest);
        } catch (HttpClientResponseException e) {//seems there is no better way to test status returned
            Assertions.assertEquals(HttpStatus.NOT_FOUND, e.getStatus());
        }
    }

    @Test
    public void create_transfer_returns_created_if_successful() {
        MoneyTransferApiRequest transferApiRequest = new MoneyTransferApiRequest(UUID.randomUUID(), UUID.randomUUID(), 55.4);
        MutableHttpRequest<MoneyTransferApiRequest> request = HttpRequest.POST("/transfers", transferApiRequest);
        HttpResponse<Object> response = client.toBlocking().exchange(request);
        Assertions.assertEquals(HttpStatus.CREATED, response.getStatus());
    }

    @Test
    public void create_transfer_returns_bad_Request_if_creation_fails() throws InvalidAccountException, InvalidTransferAmountException {
        MoneyTransferApiRequest transferApiRequest = new MoneyTransferApiRequest(UUID.randomUUID(), UUID.randomUUID(), 55.4);
        doThrow(InvalidAccountException.class).when(moneyTransferCreationService).requestMoneyTransfer(any());
        MutableHttpRequest<MoneyTransferApiRequest> request = HttpRequest.POST("/transfers", transferApiRequest);
        try {
            client.toBlocking().exchange(request);
        } catch (HttpClientResponseException e) {  //seems there is no better way to test status returned
            Assertions.assertEquals(HttpStatus.BAD_REQUEST, e.getStatus());
        }
    }

    @Test
    public void create_transfer_returns_created_transferId_if_successful() throws InvalidAccountException, InvalidTransferAmountException, JsonProcessingException {
        TransferRequestId createdTransferId = TransferRequestId.from(UUID.randomUUID());
        when(moneyTransferCreationService.requestMoneyTransfer(any())).thenReturn(createdTransferId);
        MoneyTransferApiRequest transferApiRequest = new MoneyTransferApiRequest(UUID.randomUUID(), UUID.randomUUID(), 55.4);
        MutableHttpRequest<MoneyTransferApiRequest> request = HttpRequest.POST("/transfers", transferApiRequest);
        String response = client.toBlocking().retrieve(request);
        MoneyTransferApiResponse expectedResponseBody = MoneyTransferApiResponse.builder().transferId(createdTransferId.getId()).build();
        Assertions.assertEquals(new ObjectMapper().writeValueAsString(expectedResponseBody), response);
    }

    @Test
    public void create_transfer_creates_money_transfer_with_correct_parameters() throws InvalidAccountException, InvalidTransferAmountException {
        UUID sourceAccountId = UUID.randomUUID();
        UUID beneficiaryAccountId = UUID.randomUUID();
        double transferAmount = 55.4;
        MoneyTransferApiRequest transferApiRequest = new MoneyTransferApiRequest(sourceAccountId, beneficiaryAccountId, transferAmount);
        MutableHttpRequest<MoneyTransferApiRequest> postRequest = HttpRequest.POST("/transfers", transferApiRequest);
        client.toBlocking().exchange(postRequest);

        MoneyTransferRequest expectedTransferRequest = getMoneyTransferRequest(sourceAccountId, beneficiaryAccountId, transferAmount);
        verify(moneyTransferCreationService).requestMoneyTransfer(expectedTransferRequest);
    }

    private MoneyTransferRequest getMoneyTransferRequest(UUID sourceAccountId, UUID beneficiaryAccountId, double transferAmount) {
        AccountId sourceAccountID = AccountId.from(sourceAccountId);
        AccountId beneficiaryAccountID = AccountId.from(beneficiaryAccountId);
        Amount amount = Amount.builder().amount(BigDecimal.valueOf(transferAmount)).build();
        return MoneyTransferRequest.builder().sourceAccountId(sourceAccountID).beneficiaryAccountId(beneficiaryAccountID).amount(amount).build();
    }

    @MockBean(MoneyTransferCreationService.class)
    IMoneyTransferCreationService moneyTransferCreationService() {
        return mock(IMoneyTransferCreationService.class);
    }

    @MockBean(InMemoryMoneyTransferRepository.class)
    IMoneyTransferRepository moneyTransferRepository() {
        return mock(IMoneyTransferRepository.class);
    }
}