package com.griddynamics.shopapi.service.impl;

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
  public long getUserId(HttpSession session) {
    Object sessionUserId = session.getAttribute("userId");
    if (sessionUserId == null) {
      throw new UnauthorizedException();
    }
    long userId = (long) sessionUserId;
    validateSessionInfo(userId);
    return userId;
  }

  private void validateSessionInfo(long userId) {
    Optional<User> user = userRepository.findById(userId);
    if (user.isEmpty()) {
      throw new ForbiddenResourcesException(String.format("User with id %d doesn't exist", userId));
    }
  }
}
