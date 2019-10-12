package com.revolut.challenge.api.transfer;

import com.revolut.challenge.domain.model.transfer.MoneyTransferStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.io.Serializable;
import java.util.UUID;


@Getter
@EqualsAndHashCode
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MoneyTransferDTO implements Serializable {
    private UUID sourceAccountId;
    private UUID beneficiaryAccountId;
    private MoneyTransferStatus status;
    private double amount;
}
