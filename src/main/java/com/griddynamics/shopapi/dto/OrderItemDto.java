package com.griddynamics.shopapi.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.griddynamics.shopapi.dto.validation.ValidOrderItemDto;
import com.griddynamics.shopapi.model.OrderItem;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@NoArgsConstructor
@ToString
@ValidOrderItemDto
public class OrderItemDto {

  private Integer id;
  private Long productId;
  @Positive private int quantity;

  @JsonProperty(access = JsonProperty.Access.READ_ONLY)
  private double price;

  public OrderItemDto(OrderItem item) {
    productId = item.getProductId();
    quantity = item.getQuantity();
    price = item.getPrice().doubleValue();
  }

  public OrderItemDto(OrderItem item, int index) {
    productId = item.getProductId();
    quantity = item.getQuantity();
    price = item.getPrice().doubleValue();
    id = index;
  }
}
