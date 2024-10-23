package com.griddynamics.shopapi.service;

import com.griddynamics.shopapi.dto.CartDto;
import com.griddynamics.shopapi.dto.OrderDto;
import com.griddynamics.shopapi.dto.OrderItemDto;

public interface CartService {
  CartDto getCartFor(long userId);

  CartDto deleteItemFromCart(Long id, long productId);

  CartDto updateItemAmount(OrderItemDto orderItemDto, Long id);

  OrderDto checkout(Long id);

  void clearCart(Long id);

  long getIdOfNewCartFor(long userId);
}
