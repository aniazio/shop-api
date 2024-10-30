package com.griddynamics.shopapi.dto;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

import com.griddynamics.shopapi.controller.OrderController;
import com.griddynamics.shopapi.model.OrderDetails;
import com.griddynamics.shopapi.model.OrderStatus;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.ToString;
import org.springframework.hateoas.RepresentationModel;

@Getter
@ToString
public class OrderDto extends RepresentationModel<OrderDto> {

  private final Long id;
  private final double total;
  private final OrderStatus status;
  private final Long userId;
  private final List<OrderItemDto> items = new ArrayList<>();

  public OrderDto(OrderDetails orderDetails) {
    id = orderDetails.getId();
    total = orderDetails.getTotal().doubleValue();
    status = orderDetails.getStatus();
    userId = orderDetails.getUser().getId();
    orderDetails.getItems().forEach(item -> items.add(new OrderItemDto(item)));
    this.add(linkTo(methodOn(OrderController.class).getOrderFor(id, userId, null)).withSelfRel());
  }
}
