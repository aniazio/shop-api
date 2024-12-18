package com.griddynamics.shopapi.aspect;

import jakarta.servlet.http.HttpSession;
import java.util.Arrays;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

@Aspect
@Component
@Slf4j
public class LoggingAspect {

  @Pointcut("within(com.griddynamics.shopapi.controller..*)")
  public void inControllerClass() {}

  @Before("com.griddynamics.shopapi.aspect.LoggingAspect.inControllerClass()")
  public void logBeforeControllerMethod(JoinPoint joinPoint) {
    Object lastArg = joinPoint.getArgs()[joinPoint.getArgs().length - 1];
    if (lastArg instanceof HttpSession) {
      String args =
          Arrays.stream(joinPoint.getArgs())
              .limit(joinPoint.getArgs().length - 1)
              .map(Object::toString)
              .collect(Collectors.joining(", "));
      log.debug(
          "Before method: {}, Session id: {}, Arguments: {}",
          joinPoint.getSignature().toShortString(),
          ((HttpSession) lastArg).getId(),
          args);
    } else {
      log.debug(
          "Before method: {}, Arguments: {}",
          joinPoint.getSignature().toShortString(),
          joinPoint.getArgs());
    }
  }

  @AfterReturning(
      value = "com.griddynamics.shopapi.aspect.LoggingAspect.inControllerClass()",
      returning = "retVal")
  public void logAfterControllerMethod(JoinPoint joinPoint, Object retVal) {
    Object lastArg = joinPoint.getArgs()[joinPoint.getArgs().length - 1];
    if (lastArg instanceof HttpSession) {
      String args =
          Arrays.stream(joinPoint.getArgs())
              .limit(joinPoint.getArgs().length - 1)
              .map(Object::toString)
              .collect(Collectors.joining(", "));
      log.debug(
          "After method: {}, Session id: {}, Arguments: {}, Returning: {}",
          joinPoint.getSignature().toShortString(),
          ((HttpSession) lastArg).getId(),
          args,
          retVal);
    } else {
      log.debug(
          "After method: {}, Arguments: {}, Returning: {}",
          joinPoint.getSignature().toShortString(),
          joinPoint.getArgs(),
          retVal);
    }
  }
}
