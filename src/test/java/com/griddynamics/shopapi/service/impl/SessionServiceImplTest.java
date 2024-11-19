package com.griddynamics.shopapi.service.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

import com.griddynamics.shopapi.dto.SessionInfo;
import com.griddynamics.shopapi.exception.ForbiddenResourcesException;
import com.griddynamics.shopapi.exception.UnauthorizedException;
import com.griddynamics.shopapi.model.User;
import com.griddynamics.shopapi.repository.UserRepository;
import jakarta.servlet.http.HttpSession;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SessionServiceImplTest {

  @Mock UserRepository userRepository;
  @InjectMocks SessionServiceImpl sessionService;
  @Mock HttpSession session;
  long userId = 1244L;

  @Test
  void should_authorizeAndGetSessionInfo_when_properSession() {
    when(session.getAttribute("userId")).thenReturn(userId);

    SessionInfo returned = sessionService.authorizeAndGetSessionInfo(session);

    assertEquals(userId, returned.getUserId());
  }

  @Test
  void should_getUserId() {
    when(session.getAttribute("userId")).thenReturn(userId);

    long returned = sessionService.getUserId(session);

    assertEquals(userId, returned);
  }

  @Test
  void should_throwException_when_getUserId_improperSession() {
    assertThrows(UnauthorizedException.class, () -> sessionService.getUserId(session));
  }

  @Test
  void should_notThrowException_validateSessionInfo() {
    SessionInfo sessionInfo = new SessionInfo(userId);
    when(userRepository.findById(userId)).thenReturn(Optional.of(new User()));

    sessionService.validateSessionInfo(sessionInfo);
  }

  @Test
  void should_throwException_validateSessionInfo() {
    SessionInfo sessionInfo = new SessionInfo(userId);
    when(userRepository.findById(userId)).thenReturn(Optional.empty());
    assertThrows(
        ForbiddenResourcesException.class, () -> sessionService.validateSessionInfo(sessionInfo));
  }
}
