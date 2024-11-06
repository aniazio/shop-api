package com.griddynamics.shopapi.util;

import com.griddynamics.shopapi.model.ResetToken;

public interface PasswordRessetter {

  String sendEmailWithToken(String email, ResetToken savedToken);
}
