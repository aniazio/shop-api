package com.griddynamics.shopapi.controller;

import com.griddynamics.shopapi.exception.*;
import java.net.URI;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
@Slf4j
public class GlobalControllerAdvice {

  @Value("${link.to.swagger.docs.in.repo}")
  String linkToDocs;

  @ExceptionHandler({
    CartNotFoundException.class,
    OrderNotFoundException.class,
    ProductNotFoundException.class,
    UserNotFoundException.class
  })
  public ProblemDetail handleNotFoundExceptions(Exception exception) {
    log.error("Handling " + exception.getClass());
    log.error(exception.getMessage());

    ProblemDetail problemDetail =
        ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, exception.getLocalizedMessage());

    problemDetail.setType(URI.create(linkToDocs));
    problemDetail.setTitle("Object not found");
    return problemDetail;
  }

  @ExceptionHandler(UnauthorizedException.class)
  public ProblemDetail handleUnauthorizedExceptions(Exception exception) {
    log.error("Handling " + exception.getClass());
    log.error(exception.getMessage());

    ProblemDetail problemDetail =
        ProblemDetail.forStatusAndDetail(
            HttpStatus.UNAUTHORIZED, "Unauthorized access to the resources. Please, log in");

    problemDetail.setType(URI.create(linkToDocs));
    problemDetail.setTitle("Forbidden resource");
    return problemDetail;
  }

  @ExceptionHandler(WrongCredentialsException.class)
  public ProblemDetail handleWrongCredentialsExceptions(Exception exception) {
    log.error("Handling " + exception.getClass());
    log.error(exception.getMessage());

    ProblemDetail problemDetail =
        ProblemDetail.forStatusAndDetail(
            HttpStatus.UNAUTHORIZED, "Wrong credentials. Please, try again");

    problemDetail.setType(URI.create(linkToDocs));
    problemDetail.setTitle("Wrong credentials");
    return problemDetail;
  }

  @ExceptionHandler({ForbiddenResourcesException.class})
  public ProblemDetail handleForbiddenExceptions(Exception exception) {
    log.error("Handling " + exception.getClass());
    log.error(exception.getMessage());

    ProblemDetail problemDetail =
        ProblemDetail.forStatusAndDetail(HttpStatus.FORBIDDEN, "Try to access forbidden resources");

    problemDetail.setType(URI.create(linkToDocs));
    problemDetail.setTitle("Forbidden resource");
    return problemDetail;
  }

  @ExceptionHandler({
    ConversionException.class,
    ProductNotAvailableException.class,
    WrongOrderException.class
  })
  public ProblemDetail handleBadRequestExceptions(Exception exception) {
    log.error("Handling " + exception.getClass());
    log.error(exception.getMessage());

    ProblemDetail problemDetail =
        ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, exception.getLocalizedMessage());

    problemDetail.setType(URI.create(linkToDocs));
    problemDetail.setTitle("Bad request sent");
    return problemDetail;
  }

  @ExceptionHandler(UserAlreadyExistsException.class)
  public ProblemDetail handleConflictExceptions(Exception exception) {
    log.error("Handling " + exception.getClass());
    log.error(exception.getMessage());

    ProblemDetail problemDetail =
        ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, exception.getLocalizedMessage());

    problemDetail.setType(URI.create(linkToDocs));
    problemDetail.setTitle("Invalid user data");
    return problemDetail;
  }

  @ExceptionHandler({ConstraintViolationException.class, MethodArgumentNotValidException.class})
  ProblemDetail handleValidationException(Exception exception) {
    log.error("Handling " + exception.getClass());
    log.error(exception.getMessage());

    ProblemDetail problemDetail =
        ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, exception.getLocalizedMessage());

    problemDetail.setType(URI.create(linkToDocs));
    problemDetail.setTitle("Validation error");
    return problemDetail;
  }

  @ExceptionHandler(Exception.class)
  public ProblemDetail handleOtherExceptions(Exception exception) {
    log.error("-----Handling unexpected exception " + exception.getClass());
    log.error(exception.getMessage());

    ProblemDetail problemDetail =
        ProblemDetail.forStatusAndDetail(
            HttpStatus.SERVICE_UNAVAILABLE,
            "Unexpected error on the server side. Try again later.");

    problemDetail.setType(URI.create(linkToDocs));
    problemDetail.setTitle("Server exception");
    return problemDetail;
  }
}
