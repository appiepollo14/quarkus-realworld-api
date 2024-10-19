package org.example.realworldapi.infrastructure.web.mapper;

import jakarta.ws.rs.core.Response;
import org.example.realworldapi.application.web.model.response.ErrorResponse;
import org.example.realworldapi.domain.exception.*;
import org.jboss.resteasy.reactive.server.ServerExceptionMapper;

public class ExceptionMappers {

  @ServerExceptionMapper
  public Response emailAlreadyExists(EmailAlreadyExistsException e) {
    return Response.ok(errorResponse(e)).status(Response.Status.CONFLICT.getStatusCode()).build();
  }

  @ServerExceptionMapper
  public Response usernameAlreadyExists(UsernameAlreadyExistsException e) {
    return Response.ok(errorResponse(e)).status(Response.Status.CONFLICT.getStatusCode()).build();
  }

  @ServerExceptionMapper
  public Response userNotfound(UserNotFoundException e) {
    return Response.ok(errorResponse(e)).status(Response.Status.NOT_FOUND.getStatusCode()).build();
  }

  @ServerExceptionMapper
  public Response invalidPassword(InvalidPasswordException e) {
    return Response.ok(errorResponse(e))
        .status(Response.Status.UNAUTHORIZED.getStatusCode())
        .build();
  }

  @ServerExceptionMapper
  public Response tagNotFound(TagNotFoundException e) {
    return Response.ok(errorResponse(e)).status(Response.Status.NOT_FOUND.getStatusCode()).build();
  }

  @ServerExceptionMapper
  public Response articleNotFound(ArticleNotFoundException e) {
    return Response.ok(errorResponse(e)).status(Response.Status.NOT_FOUND.getStatusCode()).build();
  }

  @ServerExceptionMapper
  public Response modelValidation(ModelValidationException e) {
    return Response.ok(errorResponse(e)).status(422).build();
  }

  private ErrorResponse errorResponse(RuntimeException e) {
    return new ErrorResponse(e.getMessage());
  }
}
