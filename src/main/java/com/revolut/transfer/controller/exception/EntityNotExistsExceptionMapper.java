package com.revolut.transfer.controller.exception;

import com.revolut.transfer.service.exception.EntityNotExistsException;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

@Provider
public class EntityNotExistsExceptionMapper implements ExceptionMapper<EntityNotExistsException> {
    @Override
    public Response toResponse(EntityNotExistsException exception) {
        return Response
                .status(Response.Status.NOT_FOUND)
                .type(MediaType.APPLICATION_JSON)
                .entity(MessageContainer.from(exception))
                .build();
    }
}
