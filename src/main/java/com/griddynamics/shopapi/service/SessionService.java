package com.griddynamics.shopapi.service;

import jakarta.servlet.http.HttpSession;

public interface SessionService {

  long getUserId(HttpSession session);
}
