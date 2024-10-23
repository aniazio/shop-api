package com.griddynamics.shopapi.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.FORBIDDEN)
public class ForbiddenResourcesException extends RuntimeException {

  public ForbiddenResourcesException() {}

  public ForbiddenResourcesException(String message) {
    super(message);
  }
}
