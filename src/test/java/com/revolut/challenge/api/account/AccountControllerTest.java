package com.revolut.challenge.api.account;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.revolut.challenge.domain.model.account.Account;
import com.revolut.challenge.domain.model.account.AccountId;
import com.revolut.challenge.domain.repositories.IAccountRepository;
import com.revolut.challenge.domain.services.account.AccountService;
import com.revolut.challenge.domain.services.account.IAccountService;
import com.revolut.challenge.infra.repositories.InMemoryAccountRepository;
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
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


@MicronautTest
class AccountControllerTest {

    @Inject
    @Client("/")
    private RxHttpClient client;
    @Inject
    private IAccountRepository accountRepository;
    @Inject
    private IAccountService accountService;

    @BeforeEach
    public void setup() {
        AccountId createdAccountId = AccountId.from(UUID.randomUUID());
        when(accountService.createAccount(any())).thenReturn(createdAccountId);
    }

    @Test
    public void get_valid_account_returns_account() throws JsonProcessingException {
        UUID validAccountId = UUID.randomUUID();
        AccountId accountId = AccountId.from(validAccountId);
        Account account = Account.builder().accountId(accountId).build();
        when(accountRepository.findById(accountId)).thenReturn(Optional.of(account));
        HttpRequest<String> request = HttpRequest.GET("/accounts/" + validAccountId);
        String body = client.toBlocking().retrieve(request);
        AccountDTO expectedResponseBody = AccountDTO.builder().currentBalance(0).accountId(validAccountId).build();
        assertNotNull(body);
        assertEquals(mapper().writeValueAsString(expectedResponseBody), body);
    }

    @Test
    public void get_valid_account_returns_ok() {
        UUID validAccountId = UUID.randomUUID();
        AccountId accountId = AccountId.from(validAccountId);
        Account account = Account.builder().accountId(accountId).build();
        when(accountRepository.findById(accountId)).thenReturn(Optional.of(account));
        HttpRequest<String> request = HttpRequest.GET("/accounts/" + validAccountId);
        HttpResponse<Object> response = client.toBlocking().exchange(request);
        assertEquals(HttpStatus.OK, response.getStatus());
    }

    @Test
    public void get_invalid_account_returns_not_found() {
        HttpRequest<String> request = HttpRequest.GET("/accounts/" + UUID.randomUUID());
        try {
            HttpResponse<Object> response = client.toBlocking().exchange(request);
        } catch (HttpClientResponseException e) {  //seems there is no better way to test status returned
            Assertions.assertEquals(HttpStatus.NOT_FOUND, e.getStatus());
        }
    }

    @Test
    public void create_account_returns_created() {
        AccountCreationApiRequest accountCreationApiRequest = new AccountCreationApiRequest(12.5);
        MutableHttpRequest<AccountCreationApiRequest> request = HttpRequest.POST("/accounts", accountCreationApiRequest);

        HttpResponse<Object> response = client.toBlocking().exchange(request);
        Assertions.assertEquals(HttpStatus.CREATED, response.getStatus());
    }

    @Test
    public void create_account_returns_account_id_created() throws JsonProcessingException {
        AccountId createdAccountId = AccountId.from(UUID.randomUUID());
        when(accountService.createAccount(any())).thenReturn(createdAccountId);

        AccountCreationApiRequest accountCreationApiRequest = new AccountCreationApiRequest(12.5);
        MutableHttpRequest<AccountCreationApiRequest> request = HttpRequest.POST("/accounts", accountCreationApiRequest);

        String response = client.toBlocking().retrieve(request);
        AccountCreationApiResponse expectedApiResponse = AccountCreationApiResponse.builder().accountId(createdAccountId.getId()).build();
        Assertions.assertEquals(new ObjectMapper().writeValueAsString(expectedApiResponse), response);
    }

    private ObjectMapper mapper() {
        return new ObjectMapper();
    }

    @MockBean(InMemoryAccountRepository.class)
    IAccountRepository accountRepository() {
        return mock(IAccountRepository.class);
    }

    @MockBean(AccountService.class)
    IAccountService accountService() {
        return mock(IAccountService.class);
    }
}