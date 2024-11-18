package com.griddynamics.shopapi.service.impl;

import com.griddynamics.shopapi.dto.SessionInfo;
import com.griddynamics.shopapi.exception.CartNotFoundException;
import com.griddynamics.shopapi.exception.ForbiddenResourcesException;
import com.griddynamics.shopapi.exception.UnauthorizedException;
import com.griddynamics.shopapi.repository.OrderRepository;
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

  private final OrderRepository orderRepository;

  @Override
  public SessionInfo authorizeAndGetSessionInfo(HttpSession session) {
    Object sessionUserId = session.getAttribute("userId");
    if (sessionUserId == null) {
      throw new UnauthorizedException();
    }
    Object sessionCartId = session.getAttribute("cartId");
    if (sessionCartId == null) {
      throw new ForbiddenResourcesException("cartId not found");
    }
    return new SessionInfo((long) sessionUserId, (long) sessionCartId);
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
    Optional<Long> userIdFromCart =
        orderRepository.findUserIdByIdAndStatusIsCart(sessionInfo.getCartId());
    if (userIdFromCart.isEmpty()) {
      throw new CartNotFoundException(
          String.format("Cart with id %d not found", sessionInfo.getCartId()));
    }
    if (!userIdFromCart.get().equals(sessionInfo.getUserId())) {
      throw new ForbiddenResourcesException(
          String.format(
              "userId %1$d doesn't match info for cart on the id %2$d",
              sessionInfo.getUserId(), sessionInfo.getCartId()));
    }
  }
}
