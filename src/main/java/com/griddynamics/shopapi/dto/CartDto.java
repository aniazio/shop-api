package com.griddynamics.shopapi.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.griddynamics.shopapi.model.*;
import jakarta.validation.constraints.Positive;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
public class CartDto {


  @JsonProperty(access = JsonProperty.Access.READ_ONLY)
  private final double total;

  @JsonProperty(access = JsonProperty.Access.READ_ONLY)
  @Positive(message = "Wrong id format")
  private final Long userId;

  @JsonProperty(access = JsonProperty.Access.READ_ONLY)
  private final List<CartItemDto> items = new ArrayList<>();

  public CartDto(Cart cart) {
    total = cart.getTotal().doubleValue();
    userId = cart.getUser().getId();
    List<CartItem> originalItems = cart.getItems();
    for (int i = 0; i < originalItems.size(); i++) {
      items.add(new CartItemDto(originalItems.get(i), i));
    }
  }
}
