package com.griddynamics.shopapi.controller;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

import com.griddynamics.shopapi.dto.OrderDto;
import com.griddynamics.shopapi.exception.ForbiddenResourcesException;
import com.griddynamics.shopapi.exception.UnauthorizedException;
import com.griddynamics.shopapi.service.OrderService;
import jakarta.servlet.http.HttpSession;
import java.util.List;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("users/{userId}/orders")
public class OrderController {

  private final OrderService orderService;

  public OrderController(OrderService orderService) {
    this.orderService = orderService;
  }

  @GetMapping("")
  public CollectionModel<OrderDto> getAllOrdersFor(@PathVariable Long userId, HttpSession session) {
    validateUserId(userId, session);
    List<OrderDto> returned = orderService.getAllOrderFor(userId);

    CollectionModel<OrderDto> response = CollectionModel.of(returned);
    response.add(linkTo(methodOn(this.getClass()).getAllOrdersFor(userId, session)).withSelfRel());
    return response;
  }

  @GetMapping("/{orderId}")
  public EntityModel<OrderDto> getOrderFor(
      @PathVariable long userId, @PathVariable long orderId, HttpSession session) {
    validateUserId(userId, session);
    OrderDto returned = orderService.getOrderFor(userId, orderId);

    EntityModel<OrderDto> response = EntityModel.of(returned);
    response.add(
        linkTo(methodOn(this.getClass()).getAllOrdersFor(userId, session)).withRel("allOrders"));
    response.add(
        linkTo(methodOn(this.getClass()).getOrderFor(userId, orderId, session)).withSelfRel());
    return response;
  }

  @DeleteMapping("/{orderId}")
  public void deleteOrder(
      @PathVariable long userId, @PathVariable long orderId, HttpSession session) {
    validateUserId(userId, session);
    orderService.deleteOrder(userId, orderId);
  }

  private void validateUserId(long userId, HttpSession session) {
    Object sessionUserId = session.getAttribute("userId");
    if (sessionUserId == null) {
      throw new UnauthorizedException();
    }
    if ((long) sessionUserId != userId) {
      throw new ForbiddenResourcesException(
          "Resource for " + userId + " is requested by session with userId: " + sessionUserId);
    }
  }
}
