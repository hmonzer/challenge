package com.revolut.challenge.api.transfer;

import com.revolut.challenge.domain.model.account.AccountId;
import com.revolut.challenge.domain.model.account.Amount;
import com.revolut.challenge.domain.model.transfer.MoneyTransfer;
import com.revolut.challenge.domain.model.transfer.TransferRequestId;
import com.revolut.challenge.domain.repositories.IMoneyTransferRepository;
import com.revolut.challenge.domain.services.account.InvalidAccountException;
import com.revolut.challenge.domain.services.transfer.IMoneyTransferCreationService;
import com.revolut.challenge.domain.services.transfer.InvalidTransferAmountException;
import com.revolut.challenge.domain.services.transfer.MoneyTransferRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.annotation.Body;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.Post;
import io.micronaut.http.hateoas.JsonError;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;
import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


@Slf4j
@Controller("/transfers")
public class MoneyTransferController {
    private IMoneyTransferCreationService moneyTransferCreationService;
    private IMoneyTransferRepository moneyTransferRepository;

    @Inject
    public MoneyTransferController(IMoneyTransferCreationService moneyTransferCreationService, IMoneyTransferRepository moneyTransferRepository) {
        this.moneyTransferCreationService = moneyTransferCreationService;
        this.moneyTransferRepository = moneyTransferRepository;
    }

    @Get("{transferId}")
    public HttpResponse getMoneyTransferById(UUID transferId) {
        Optional<MoneyTransfer> moneyTransfer = moneyTransferRepository.findById(TransferRequestId.from(transferId));
        return moneyTransfer.isPresent() ? HttpResponse.ok(toDTO(moneyTransfer.get())) : HttpResponse.notFound(new JsonError("Transfer Request Not Found"));
    }

    @Post
    public HttpResponse createMoneyTransfer(@Body MoneyTransferApiRequest request) {
        try {
            TransferRequestId transferRequestId = requestMoneyTransfer(request);
            return HttpResponse.created(MoneyTransferApiResponse.builder().transferId(transferRequestId.getId()).build());
        } catch (InvalidAccountException | InvalidTransferAmountException e) {
            log.error("Failed to create Money Transfer {}", request, e);
            return HttpResponse.badRequest(new JsonError(e.getMessage()));
        }
    }

    private TransferRequestId requestMoneyTransfer(@Body MoneyTransferApiRequest request) throws InvalidAccountException, InvalidTransferAmountException {
        return moneyTransferCreationService.requestMoneyTransfer(toMoneyTransferRequest(request));
    }

    private MoneyTransferRequest toMoneyTransferRequest(MoneyTransferApiRequest apiRequest) {
        AccountId sourceAccountID = AccountId.from(apiRequest.getSourceAccountId());
        AccountId beneficiaryAccountID = AccountId.from(apiRequest.getBeneficiaryAccountId());
        Amount amount = Amount.builder().amount(BigDecimal.valueOf(apiRequest.getTransferAmount())).build();
        return MoneyTransferRequest.builder().sourceAccountId(sourceAccountID).beneficiaryAccountId(beneficiaryAccountID).amount(amount).build();
    }

    private MoneyTransferDTO toDTO(MoneyTransfer moneyTransfer) {
        return MoneyTransferDTO.builder().sourceAccountId(moneyTransfer.getSourceAccountId().getId()).beneficiaryAccountId(moneyTransfer.getBeneficiaryAccountId().getId())
                               .amount(moneyTransfer.getAmount().getAmount().doubleValue()).status(moneyTransfer.getStatus()).build();
    }
}
