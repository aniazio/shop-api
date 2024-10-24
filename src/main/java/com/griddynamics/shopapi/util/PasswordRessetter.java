package com.griddynamics.shopapi.util;

public interface PasswordRessetter {

  void sendEmailWithToken(String email);
}
