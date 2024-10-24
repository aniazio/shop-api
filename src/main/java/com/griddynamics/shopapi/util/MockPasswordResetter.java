package com.griddynamics.shopapi.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class MockPasswordResetter implements PasswordRessetter {

  @Override
  public void sendEmailWithToken(String email) {
    log.info("Mock - sending email for resetting password");
  }
}
