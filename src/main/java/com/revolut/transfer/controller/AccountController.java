package com.revolut.transfer.controller;

import com.revolut.transfer.controller.request.CreateAccountRequest;
import com.revolut.transfer.model.Account;
import com.revolut.transfer.repository.Repository;
import com.revolut.transfer.service.exception.EntityNotExistsException;
import lombok.RequiredArgsConstructor;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.math.BigDecimal;
import java.util.List;

@Path("/account")
@RequiredArgsConstructor
public class AccountController {

    private final Repository<Account> accountRepository;

    @GET
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Account getAccount(@PathParam("id") Long accountId) {
        return accountRepository.get(accountId)
                .orElseThrow(() -> EntityNotExistsException.accountNotFoundById(accountId));

    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public List<Account> getAll() {
        return accountRepository.getAll();
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Account createAccount(CreateAccountRequest request) {
        validateRequest(request);
        var account = new Account();
        account.setName(request.getName());
        account.setBalance(request.getBalance());
        return accountRepository.create(account);
    }

    private void validateRequest(CreateAccountRequest request) {
        if (request == null) throw new IllegalArgumentException("Incoming request cannot be null");
        if (request.getName() == null || request.getName().isEmpty())
            throw new IllegalArgumentException("Account name cannot be null or empty");
        if (request.getBalance() == null) throw new IllegalArgumentException("Account balance cannot be null");
        if (request.getBalance().compareTo(BigDecimal.ZERO) < 0)
            throw new IllegalArgumentException("Account balance cannot be negative");
    }
}
