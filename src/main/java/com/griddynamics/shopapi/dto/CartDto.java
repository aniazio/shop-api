package com.griddynamics.shopapi.dto;

import com.griddynamics.shopapi.exception.ConversionException;
import com.griddynamics.shopapi.model.OrderDetails;
import com.griddynamics.shopapi.model.OrderStatus;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
public class CartDto {

  private final Long id;
  private final double total;
  private final Long clientId;
  private final List<OrderItemDto> items = new ArrayList<>();

  public CartDto(OrderDetails orderDetails) {
    if (orderDetails.getStatus() != OrderStatus.CART) {
      throw new ConversionException(
          "A try to convert order with inappropriate status to a cart object");
    }
    id = orderDetails.getId();
    total = orderDetails.getTotal();
    clientId = orderDetails.getClient().getId();
    orderDetails.getItems().forEach(item -> items.add(new OrderItemDto(item)));
  }
}
