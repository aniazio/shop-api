package com.griddynamics.shopapi.controller;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

import com.griddynamics.shopapi.dto.OrderDto;
import com.griddynamics.shopapi.exception.UnauthorizedException;
import com.griddynamics.shopapi.service.OrderService;
import jakarta.servlet.http.HttpSession;
import java.util.List;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/orders")
public class OrderController {

  private final OrderService orderService;

  public OrderController(OrderService orderService) {
    this.orderService = orderService;
  }

  @GetMapping("")
  public CollectionModel<OrderDto> getAllOrders(HttpSession session) {
    long userId = getUserId(session);
    List<OrderDto> returned = orderService.getAllOrderForUser(userId);

    CollectionModel<OrderDto> response = CollectionModel.of(returned);
    response.add(linkTo(methodOn(this.getClass()).getAllOrders(session)).withSelfRel());
    return response;
  }

  @GetMapping("/{orderId}")
  public EntityModel<OrderDto> getOrder(@PathVariable long orderId, HttpSession session) {
    long userId = getUserId(session);
    OrderDto returned = orderService.getOrderForUser(userId, orderId);

    EntityModel<OrderDto> response = EntityModel.of(returned);
    response.add(linkTo(methodOn(this.getClass()).getAllOrders(session)).withRel("allOrders"));
    response.add(linkTo(methodOn(this.getClass()).getOrder(orderId, session)).withSelfRel());
    return response;
  }

  @DeleteMapping("/{orderId}")
  public void deleteOrder(@PathVariable long orderId, HttpSession session) {
    long userId = getUserId(session);
    orderService.cancelOrder(userId, orderId);
  }

  private Long getUserId(HttpSession session) {
    Object sessionUserId = session.getAttribute("userId");
    if (sessionUserId == null) {
      throw new UnauthorizedException();
    }
    return (long) sessionUserId;
  }
}
