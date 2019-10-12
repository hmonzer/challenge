package com.revolut.challenge.api.transfer;

import io.micronaut.http.annotation.Get;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.io.Serializable;
import java.util.UUID;


@NoArgsConstructor
@AllArgsConstructor
@Getter
@ToString
@EqualsAndHashCode
public class MoneyTransferApiRequest implements Serializable {
    private UUID sourceAccountId;
    private UUID beneficiaryAccountId;
    private double transferAmount;
}
