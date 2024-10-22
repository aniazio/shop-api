package com.griddynamics.shopapi.security;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

public class Encoder {
  private static PasswordEncoder encoder = new BCryptPasswordEncoder();

  public static String encode(String str) {
    return encoder.encode(str);
  }
}