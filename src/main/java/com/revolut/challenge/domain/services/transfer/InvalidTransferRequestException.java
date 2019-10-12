package com.revolut.challenge.domain.services.transfer;

import com.revolut.challenge.domain.model.transfer.TransferRequestId;


public class InvalidTransferRequestException extends Exception {

    public InvalidTransferRequestException(TransferRequestId requestId) {
        super(String.format("Invalid Transfer Request %s", requestId.getId().toString()));
    }
}
