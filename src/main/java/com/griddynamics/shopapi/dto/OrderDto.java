package com.griddynamics.shopapi.dto;

import com.griddynamics.shopapi.model.OrderDetails;
import com.griddynamics.shopapi.model.OrderStatus;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
public class OrderDto {

  private final Long id;
  private final double total;
  private final OrderStatus status;
  private final Long client_id;
  private final List<OrderItemDto> items = new ArrayList<>();

  public OrderDto(OrderDetails orderDetails) {
    id = orderDetails.getId();
    total = orderDetails.getTotal();
    status = orderDetails.getStatus();
    client_id = orderDetails.getClient().getId();
    orderDetails.getItems().forEach(item -> items.add(new OrderItemDto(item)));
  }
}
