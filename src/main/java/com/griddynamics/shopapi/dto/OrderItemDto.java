package com.griddynamics.shopapi.dto;

import com.griddynamics.shopapi.model.OrderItem;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@NoArgsConstructor
@ToString
public class OrderItemDto {

  private Long productId;
  private int quantity;
  private double price;

  public OrderItemDto(OrderItem item) {
    productId = item.getProductId();
    quantity = item.getQuantity();
    price = item.getPrice().doubleValue();
  }
}
