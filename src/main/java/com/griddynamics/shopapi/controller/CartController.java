package com.griddynamics.shopapi.controller;

import com.griddynamics.shopapi.dto.CartDto;
import com.griddynamics.shopapi.dto.OrderDto;
import com.griddynamics.shopapi.dto.OrderItemDto;
import com.griddynamics.shopapi.dto.SessionInfo;
import com.griddynamics.shopapi.exception.ForbiddenResourcesException;
import com.griddynamics.shopapi.service.CartService;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/cart")
public class CartController {
  private final CartService cartService;

  public CartController(CartService cartService) {
    this.cartService = cartService;
  }

  @GetMapping("")
  public CartDto getCart(HttpSession session) {
    SessionInfo sessionInfo = getSessionInfo(session);
    return cartService.getCartFor(sessionInfo);
  }

  @DeleteMapping("/{productId}")
  public void deleteItemFromCart(@PathVariable long productId, HttpSession session) {
    SessionInfo sessionInfo = getSessionInfo(session);
    cartService.deleteItemFromCart(productId, sessionInfo);
  }

  @PostMapping("/add-item")
  public void addItemToCart(@RequestBody OrderItemDto orderItemDto, HttpSession session) {
    SessionInfo sessionInfo = getSessionInfo(session);
    cartService.addItem(orderItemDto, sessionInfo);
  }

  @PatchMapping("/update-item")
  public void updateItemAmount(@RequestBody OrderItemDto orderItemDto, HttpSession session) {
    SessionInfo sessionInfo = getSessionInfo(session);
    cartService.updateItemAmount(orderItemDto, sessionInfo);
  }

  @PutMapping("/checkout")
  public OrderDto checkout(HttpSession session) {
    SessionInfo sessionInfo = getSessionInfo(session);
    OrderDto order = cartService.checkout(sessionInfo);
    session.setAttribute("cartId", cartService.getIdOfNewCart(sessionInfo));
    return order;
  }

  @DeleteMapping("")
  public void clearCart(HttpSession session) {
    SessionInfo sessionInfo = getSessionInfo(session);
    cartService.clearCart(sessionInfo);
  }

  private SessionInfo getSessionInfo(HttpSession session) {
    Object sessionUserId = session.getAttribute("clientId");
    if (sessionUserId == null) {
      throw new ForbiddenResourcesException(
          "userId not found in session for the method, which it requires.");
    }
    Object sessionCartId = session.getAttribute("cartId");
    if (sessionCartId == null) {
      throw new ForbiddenResourcesException(
          "cartId not found in session for the method, which it requires.");
    }
    return new SessionInfo((long) sessionUserId, (long) sessionCartId);
  }
}
