package com.griddynamics.shopapi.controller;

import com.griddynamics.shopapi.dto.OrderDto;
import com.griddynamics.shopapi.dto.OrderListDto;
import com.griddynamics.shopapi.exceptions.ForbiddenResourcesException;
import com.griddynamics.shopapi.service.OrderService;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("users/{userId}/orders")
public class OrderController {

  private final OrderService orderService;

  public OrderController(OrderService orderService) {
    this.orderService = orderService;
  }

  @GetMapping("")
  public OrderListDto getAllOrdersFor(@PathVariable long userId, HttpSession session) {
    validateUserId(userId, session);
    return orderService.getAllOrderFor(userId);
  }

  @GetMapping("/{orderId}")
  public OrderDto getOrderFor(
      @PathVariable long userId, @PathVariable long orderId, HttpSession session) {
    validateUserId(userId, session);
    return orderService.getOrderFor(userId, orderId);
  }

  @DeleteMapping("/{orderId}")
  public void deleteOrder(
      @PathVariable long userId, @PathVariable long orderId, HttpSession session) {
    validateUserId(userId, session);
    orderService.deleteOrder(userId, orderId);
  }

  private void validateUserId(long userId, HttpSession session) {
    Object sessionUserId = session.getAttribute("userId");
    if (sessionUserId == null || (long) sessionUserId != userId) {
      throw new ForbiddenResourcesException(
          "Resource for " + userId + " is requested by session with userId: " + sessionUserId);
    }
  }
}
