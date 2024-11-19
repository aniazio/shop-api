package com.griddynamics.shopapi.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.griddynamics.shopapi.dto.validation.ValidCartItemDto;
import com.griddynamics.shopapi.model.CartItem;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@NoArgsConstructor
@ToString
@ValidCartItemDto
public class CartItemDto {

  private Integer ordinal;
  private Long productId;

  @JsonProperty(access = JsonProperty.Access.READ_ONLY)
  private String name;

  @Positive(message = "Quantity must be positive")
  private int quantity;

  @JsonProperty(access = JsonProperty.Access.READ_ONLY)
  private double price;

  public CartItemDto(CartItem item) {
    productId = item.getProductId();
    name = item.getProduct().getTitle();
    quantity = item.getQuantity();
    price = item.getPrice().doubleValue();
  }

  public CartItemDto(CartItemDto cartItemDto) {
    this.ordinal = cartItemDto.getOrdinal();
    this.productId = cartItemDto.getProductId();
    this.name = cartItemDto.getName();
    this.quantity = cartItemDto.getQuantity();
    this.price = cartItemDto.getPrice();
  }

  public CartItemDto(CartItem item, int ordinal) {
    productId = item.getProductId();
    name = item.getProduct().getTitle();
    quantity = item.getQuantity();
    price = item.getPrice().doubleValue();
    this.ordinal = ordinal;
  }
}
