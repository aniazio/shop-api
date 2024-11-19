package com.griddynamics.shopapi.service.impl;

import com.griddynamics.shopapi.dto.SessionInfo;
import com.griddynamics.shopapi.exception.ForbiddenResourcesException;
import com.griddynamics.shopapi.exception.UnauthorizedException;
import com.griddynamics.shopapi.model.User;
import com.griddynamics.shopapi.repository.UserRepository;
import com.griddynamics.shopapi.service.SessionService;
import jakarta.servlet.http.HttpSession;
import jakarta.transaction.Transactional;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Transactional
public class SessionServiceImpl implements SessionService {

  private final UserRepository userRepository;

  @Override
  public SessionInfo authorizeAndGetSessionInfo(HttpSession session) {
    Object sessionUserId = session.getAttribute("userId");
    if (sessionUserId == null) {
      throw new UnauthorizedException();
    }
    return new SessionInfo((long) sessionUserId);
  }

  @Override
  public Long getUserId(HttpSession session) {
    Object sessionUserId = session.getAttribute("userId");
    if (sessionUserId == null) {
      throw new UnauthorizedException();
    }
    return (long) sessionUserId;
  }

  @Override
  public void validateSessionInfo(SessionInfo sessionInfo) {
    Optional<User> user = userRepository.findById(sessionInfo.getUserId());
    if (user.isEmpty()) {
      throw new ForbiddenResourcesException(
          String.format("User with id %d doesn't exist", sessionInfo.getUserId()));
    }
  }
}
