package com.griddynamics.shopapi.controller;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

import com.griddynamics.shopapi.dto.OrderDto;
import com.griddynamics.shopapi.service.OrderService;
import com.griddynamics.shopapi.service.SessionService;
import jakarta.servlet.http.HttpSession;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/orders")
@Slf4j
@RequiredArgsConstructor
public class OrderController {

  private final OrderService orderService;
  private final SessionService sessionService;

  @GetMapping
  public CollectionModel<OrderDto> getAllOrders(HttpSession session) {
    log.debug("OrderController.getAllOrders; Request received for session id {}", session.getId());
    long userId = sessionService.getUserId(session);
    List<OrderDto> orderDtos = orderService.getAllOrderForUser(userId);

    CollectionModel<OrderDto> response = CollectionModel.of(orderDtos);
    response.add(linkTo(methodOn(this.getClass()).getAllOrders(session)).withSelfRel());
    log.debug(
        "OrderController.getAllOrders; Response sent for session id {}: {}",
        session.getId(),
        orderDtos);
    return response;
  }

  @GetMapping("/{orderId}")
  public EntityModel<OrderDto> getOrder(@PathVariable long orderId, HttpSession session) {
    log.debug(
        "OrderController.getOrder; Request received for session id {}: orderId = {}",
        session.getId(),
        orderId);
    long userId = sessionService.getUserId(session);
    OrderDto orderDto = orderService.getOrderForUser(userId, orderId);

    EntityModel<OrderDto> response = EntityModel.of(orderDto);
    response.add(linkTo(methodOn(this.getClass()).getAllOrders(session)).withRel("allOrders"));
    response.add(linkTo(methodOn(this.getClass()).getOrder(orderId, session)).withSelfRel());

    log.debug(
        "OrderController.getOrder; Response sent for session id {}: {}", session.getId(), orderDto);
    return response;
  }

  @DeleteMapping("/{orderId}")
  public void cancelOrder(@PathVariable long orderId, HttpSession session) {
    log.debug(
        "OrderController.cancelOrder; Request received for session id {}: orderId = {}",
        session.getId(),
        orderId);
    long userId = sessionService.getUserId(session);
    orderService.cancelOrder(userId, orderId);
    log.debug("OrderController.cancelOrder; Response sent for session id {}", session.getId());
  }


}
