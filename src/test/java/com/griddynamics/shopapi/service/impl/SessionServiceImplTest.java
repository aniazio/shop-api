package com.griddynamics.shopapi.service.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

import com.griddynamics.shopapi.dto.SessionInfo;
import com.griddynamics.shopapi.exception.CartNotFoundException;
import com.griddynamics.shopapi.exception.ForbiddenResourcesException;
import com.griddynamics.shopapi.exception.UnauthorizedException;
import com.griddynamics.shopapi.repository.OrderRepository;
import jakarta.servlet.http.HttpSession;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SessionServiceImplTest {

  @Mock OrderRepository orderRepository;
  @InjectMocks SessionServiceImpl sessionService;
  @Mock HttpSession session;
  long userId = 1244L;
  long cartId = 32L;

  @Test
  void should_authorizeAndGetSessionInfo_when_properSession() {
    when(session.getAttribute("userId")).thenReturn(userId);
    when(session.getAttribute("cartId")).thenReturn(cartId);

    SessionInfo returned = sessionService.authorizeAndGetSessionInfo(session);

    assertEquals(userId, returned.getUserId());
    assertEquals(cartId, returned.getCartId());
  }

  @Test
  void should_throwException_when_authorizeAndGetSessionInfo_improperSession() {

    assertThrows(
        UnauthorizedException.class, () -> sessionService.authorizeAndGetSessionInfo(session));

    when(session.getAttribute("userId")).thenReturn(userId);
    assertThrows(
        ForbiddenResourcesException.class,
        () -> sessionService.authorizeAndGetSessionInfo(session));
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
    SessionInfo sessionInfo = new SessionInfo(userId, cartId);
    when(orderRepository.findUserIdByIdAndStatusIsCart(cartId)).thenReturn(Optional.of(userId));

    sessionService.validateSessionInfo(sessionInfo);
  }

  @Test
  void should_throwException_validateSessionInfo() {
    SessionInfo sessionInfo = new SessionInfo(userId, cartId);
    when(orderRepository.findUserIdByIdAndStatusIsCart(cartId)).thenReturn(Optional.empty());
    assertThrows(
        CartNotFoundException.class, () -> sessionService.validateSessionInfo(sessionInfo));

    when(orderRepository.findUserIdByIdAndStatusIsCart(cartId)).thenReturn(Optional.of(userId + 1));
    assertThrows(
        ForbiddenResourcesException.class, () -> sessionService.validateSessionInfo(sessionInfo));
  }
}
