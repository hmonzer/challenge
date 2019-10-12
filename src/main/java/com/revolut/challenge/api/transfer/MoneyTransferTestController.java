package com.revolut.challenge.api.transfer;

import com.revolut.challenge.domain.model.account.AccountId;
import com.revolut.challenge.domain.model.account.Amount;
import com.revolut.challenge.domain.services.account.InvalidAccountException;
import com.revolut.challenge.domain.services.transfer.IMoneyTransferCreationService;
import com.revolut.challenge.domain.services.transfer.InvalidTransferAmountException;
import com.revolut.challenge.domain.services.transfer.MoneyTransferRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.annotation.Body;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Post;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


@Slf4j
@Controller("/test/transfers")
public class MoneyTransferTestController {
    private IMoneyTransferCreationService moneyTransferCreationService;

    public MoneyTransferTestController(IMoneyTransferCreationService moneyTransferCreationService) {
        this.moneyTransferCreationService = moneyTransferCreationService;
    }

    @Post("{number}")
    public HttpResponse createMoneyTransfer(int number, @Body MoneyTransferApiRequest request) {
        ExecutorService executor = Executors.newFixedThreadPool(number);
        for (int i = 0; i < number; i++) {
            int iterationNumber = i;
            executor.submit(() -> requestMoneyTransfer(toMoneyTransferRequest(request), iterationNumber));
        }
        log.info("All money transfer requests were created");
        return HttpResponse.ok();
    }

    private MoneyTransferRequest toMoneyTransferRequest(MoneyTransferApiRequest apiRequest) {
        AccountId sourceAccountID = AccountId.from(apiRequest.getSourceAccountId());
        AccountId beneficiaryAccountID = AccountId.from(apiRequest.getBeneficiaryAccountId());
        Amount amount = Amount.builder().amount(BigDecimal.valueOf(apiRequest.getTransferAmount())).build();
        return MoneyTransferRequest.builder().sourceAccountId(sourceAccountID).beneficiaryAccountId(beneficiaryAccountID).amount(amount).build();
    }

    private void requestMoneyTransfer(MoneyTransferRequest transferRequest, int iterationNumber) {
        try {
            moneyTransferCreationService.requestMoneyTransfer(transferRequest);
        } catch (InvalidAccountException | InvalidTransferAmountException e) {
            log.error("Failed to transfer in attempt {}", iterationNumber);
        }
    }
}
