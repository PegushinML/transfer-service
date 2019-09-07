package com.revolut.transfer.controller;

import com.revolut.transfer.controller.request.TransferRequest;
import com.revolut.transfer.model.TransferTransaction;
import com.revolut.transfer.service.TransferOperationService;
import lombok.RequiredArgsConstructor;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path("/transfer")
@RequiredArgsConstructor
public class TransferController {

    private final TransferOperationService transferService;

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public TransferTransaction transfer(TransferRequest request) {
        validateRequest(request);
        return transferService.transfer(request.getFromId(), request.getToId(), request.getAmount());
    }

    private void validateRequest(TransferRequest request) {
        if (request == null) throw new IllegalArgumentException("Incoming request cannot be null");
        if (request.getFromId() == null) throw new IllegalArgumentException("From id cannot be null");
        if (request.getToId() == null) throw new IllegalArgumentException("To id cannot be null");
        if (request.getAmount() == null) throw new IllegalArgumentException("Amount of transfer cannot be null");
    }
}
