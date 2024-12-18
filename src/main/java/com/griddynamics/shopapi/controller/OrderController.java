package com.griddynamics.shopapi.controller;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

import com.griddynamics.shopapi.dto.OrderDto;
import com.griddynamics.shopapi.service.OrderService;
import com.griddynamics.shopapi.service.SessionService;
import jakarta.servlet.http.HttpSession;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
public class OrderController {

  private final OrderService orderService;
  private final SessionService sessionService;

  @GetMapping
  public CollectionModel<OrderDto> getAllOrders(HttpSession session) {
    long userId = sessionService.getUserId(session);
    List<OrderDto> orderDtos = orderService.getAllOrderForUser(userId);

    CollectionModel<OrderDto> response = CollectionModel.of(orderDtos);
    response.add(linkTo(methodOn(this.getClass()).getAllOrders(session)).withSelfRel());
    return response;
  }

  @GetMapping("/{orderId}")
  public EntityModel<OrderDto> getOrder(@PathVariable long orderId, HttpSession session) {
    long userId = sessionService.getUserId(session);
    OrderDto orderDto = orderService.getOrderForUser(userId, orderId);

    EntityModel<OrderDto> response = EntityModel.of(orderDto);
    response.add(linkTo(methodOn(this.getClass()).getAllOrders(session)).withRel("allOrders"));
    response.add(linkTo(methodOn(this.getClass()).getOrder(orderId, session)).withSelfRel());

    return response;
  }

  @DeleteMapping("/{orderId}")
  public void cancelOrder(@PathVariable long orderId, HttpSession session) {
    long userId = sessionService.getUserId(session);
    orderService.cancelOrder(userId, orderId);
  }


}
