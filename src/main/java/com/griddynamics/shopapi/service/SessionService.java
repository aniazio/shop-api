package com.griddynamics.shopapi.service;

import com.griddynamics.shopapi.dto.SessionInfo;
import jakarta.servlet.http.HttpSession;

public interface SessionService {
  SessionInfo authorizeAndGetSessionInfo(HttpSession session);

  Long getUserId(HttpSession session);

  void validateSessionInfo(SessionInfo sessionInfo);
}
