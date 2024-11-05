package com.griddynamics.shopapi.controller;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

import com.griddynamics.shopapi.exception.*;
import jakarta.servlet.http.HttpSession;
import java.util.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
@Slf4j
public class GlobalControllerAdvice {

  @ExceptionHandler(CartNotFoundException.class)
  public ProblemDetail handleCartNotFoundExceptions(Exception exception) {
    log.error("Handling " + exception.getClass());
    log.error(exception.getMessage());

    ProblemDetail problemDetail =
        ProblemDetail.forStatusAndDetail(
            HttpStatus.NOT_FOUND, "Your cart is not found. Please try to log in again");

    problemDetail.setTitle("Resource not found");
    return problemDetail;
  }

  @ExceptionHandler({
    OrderNotFoundException.class,
    ProductNotFoundException.class,
    UserNotFoundException.class
  })
  public ProblemDetail handleNotFoundExceptions(Exception exception) {
    log.error("Handling " + exception.getClass());
    log.error(exception.getMessage());

    ProblemDetail problemDetail =
        ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, exception.getLocalizedMessage());

    problemDetail.setTitle("Resource not found");
    return problemDetail;
  }

  @ExceptionHandler(UnauthorizedException.class)
  public ProblemDetail handleUnauthorizedExceptions(HttpSession session, Exception exception) {
    log.error("Handling " + exception.getClass());
    log.error(exception.getMessage());

    ProblemDetail problemDetail =
        ProblemDetail.forStatusAndDetail(
            HttpStatus.UNAUTHORIZED, "Unauthorized access to the resources. Please, log in");

    problemDetail.setTitle("Unauthorized");
    problemDetail.setProperty(
        "loginForm", linkTo(methodOn(UserController.class).loginUser(null, session)).toString());
    return problemDetail;
  }

  @ExceptionHandler(WrongCredentialsException.class)
  public ProblemDetail handleWrongCredentialsExceptions(Exception exception) {
    log.error("Handling " + exception.getClass());
    log.error(exception.getMessage());

    ProblemDetail problemDetail =
        ProblemDetail.forStatusAndDetail(
            HttpStatus.UNAUTHORIZED, "Wrong credentials. Please, try again");

    problemDetail.setTitle("Wrong credentials");
    problemDetail.setProperty(
        "registerForm", linkTo(methodOn(UserController.class).registerUser(null)).toString());
    problemDetail.setProperty(
        "resetPasswordForm",
        linkTo(methodOn(UserController.class).requestPasswordReset(null)).toString());
    return problemDetail;
  }

  @ExceptionHandler({ForbiddenResourcesException.class})
  public ProblemDetail handleForbiddenExceptions(HttpSession session, Exception exception) {
    log.error("Handling " + exception.getClass());
    log.error(exception.getMessage());

    ProblemDetail problemDetail =
        ProblemDetail.forStatusAndDetail(HttpStatus.FORBIDDEN, "Try to access forbidden resources");

    problemDetail.setTitle("Forbidden resource");
    problemDetail.setProperty(
        "loginForm", linkTo(methodOn(UserController.class).loginUser(null, session)).toString());
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

    problemDetail.setTitle("Bad request sent");
    return problemDetail;
  }

  @ExceptionHandler(UserAlreadyExistsException.class)
  public ProblemDetail handleConflictExceptions(Exception exception) {
    log.error("Handling " + exception.getClass());
    log.error(exception.getMessage());

    ProblemDetail problemDetail =
        ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, exception.getLocalizedMessage());

    problemDetail.setTitle("Invalid user data");
    return problemDetail;
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  ProblemDetail handleValidationException(MethodArgumentNotValidException exception) {
    StringBuilder message = new StringBuilder();
    exception
        .getBindingResult()
        .getAllErrors()
        .forEach(
            (error) -> {
              message.append(" ").append(error.getDefaultMessage());
            });

    message.deleteCharAt(0);

    log.error("Handling " + exception.getClass());
    log.error(exception.getMessage());

    ProblemDetail problemDetail =
        ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, message.toString());

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

    problemDetail.setTitle("Server exception");
    return problemDetail;
  }
}
