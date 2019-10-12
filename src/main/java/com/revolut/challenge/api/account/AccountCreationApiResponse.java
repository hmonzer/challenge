package com.revolut.challenge.api.account;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.io.Serializable;
import java.util.UUID;


@NoArgsConstructor
@Builder
@AllArgsConstructor
@Getter
@EqualsAndHashCode
@ToString
public class AccountCreationApiResponse implements Serializable {
    private UUID accountId;
}
