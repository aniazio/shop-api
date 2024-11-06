package com.griddynamics.shopapi.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.griddynamics.shopapi.exception.ConversionException;
import com.griddynamics.shopapi.model.OrderDetails;
import com.griddynamics.shopapi.model.OrderItem;
import com.griddynamics.shopapi.model.OrderStatus;
import jakarta.validation.constraints.Positive;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
public class CartDto {

  @JsonProperty(access = JsonProperty.Access.READ_ONLY)
  @Positive(message = "Wrong id format")
  private final Long id;

  @JsonProperty(access = JsonProperty.Access.READ_ONLY)
  private final double total;

  @JsonIgnore private final Long userId;

  @JsonProperty(access = JsonProperty.Access.READ_ONLY)
  private final List<OrderItemDto> items = new ArrayList<>();

  public CartDto(OrderDetails orderDetails) {
    if (orderDetails.getStatus() != OrderStatus.CART) {
      throw new ConversionException(
          "A try to convert order with inappropriate status to a cart object");
    }
    id = orderDetails.getId();
    total = orderDetails.getTotal().doubleValue();
    userId = orderDetails.getUser().getId();
    List<OrderItem> originalItems = orderDetails.getItems();
    for (int i = 0; i < originalItems.size(); i++) {
      items.add(new OrderItemDto(originalItems.get(i), i));
    }
  }
}
