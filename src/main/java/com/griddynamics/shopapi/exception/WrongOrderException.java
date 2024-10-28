package com.griddynamics.shopapi.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class WrongOrderException extends RuntimeException {

  public WrongOrderException(String message) {
    super(message);
  }
}
