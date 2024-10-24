package com.griddynamics.shopapi.util;

import com.griddynamics.shopapi.model.ResetToken;

public interface PasswordRessetter {

  void sendEmailWithToken(String email, ResetToken savedToken);
}
