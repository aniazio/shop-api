package com.griddynamics.shopapi.dto;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.griddynamics.shopapi.controller.OrderController;
import com.griddynamics.shopapi.model.OrderDetails;
import com.griddynamics.shopapi.model.OrderStatus;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.ToString;
import org.springframework.hateoas.RepresentationModel;

@Getter
@ToString
public class OrderDto extends RepresentationModel<OrderDto> {
  @JsonProperty(access = JsonProperty.Access.READ_ONLY)
  @Positive
  private final Long id;

  @JsonProperty(access = JsonProperty.Access.READ_ONLY)
  @PositiveOrZero
  private final double total;

  @JsonProperty(access = JsonProperty.Access.READ_ONLY)
  private final OrderStatus status;

  @JsonIgnore @Positive private final Long userId;
  private final List<OrderItemDto> items = new ArrayList<>();

  public OrderDto(OrderDetails orderDetails) {
    id = orderDetails.getId();
    total = orderDetails.getTotal().doubleValue();
    status = orderDetails.getStatus();
    userId = orderDetails.getUser().getId();
    orderDetails.getItems().forEach(item -> items.add(new OrderItemDto(item)));
    this.add(linkTo(methodOn(OrderController.class).getOrder(id, null)).withSelfRel());
  }
}
