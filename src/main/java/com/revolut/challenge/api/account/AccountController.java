package com.revolut.challenge.api.account;

import com.revolut.challenge.domain.model.account.Account;
import com.revolut.challenge.domain.model.account.AccountId;
import com.revolut.challenge.domain.model.account.Amount;
import com.revolut.challenge.domain.repositories.IAccountRepository;
import com.revolut.challenge.domain.services.account.IAccountService;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.annotation.Body;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.Post;
import io.micronaut.http.hateoas.JsonError;

import javax.inject.Inject;
import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;


@Controller("/accounts")
public class AccountController {

    private IAccountRepository accountRepository;
    private IAccountService accountService;

    @Inject
    public AccountController(IAccountRepository accountRepository, IAccountService accountService) {
        this.accountRepository = accountRepository;
        this.accountService = accountService;
    }

    @Get("/{accountId}")
    public HttpResponse findAccountById(UUID accountId) {
        Optional<Account> account = accountRepository.findById(AccountId.from(accountId));
        return account.isPresent() ? HttpResponse.ok(toDTO(account.get())) : HttpResponse.notFound(new JsonError("Account Not Found"));
    }

    @Post
    public HttpResponse createAccount(@Body AccountCreationApiRequest request) {
        AccountId accountId = accountService.createAccount(Amount.builder().amount(BigDecimal.valueOf(request.getInitialAmount())).build());
        return HttpResponse.created(AccountCreationApiResponse.builder().accountId(accountId.getId()).build());
    }

    private AccountDTO toDTO(Account account) {
        return AccountDTO.builder().accountId(account.getAccountId().getId()).currentBalance(account.getCurrentBalance().getAmount().doubleValue()).build();
    }
}
