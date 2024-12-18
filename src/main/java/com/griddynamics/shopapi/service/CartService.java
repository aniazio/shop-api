package com.griddynamics.shopapi.service;

import com.griddynamics.shopapi.dto.*;

public interface CartService {

  CartDto getCartFor(long userId);

  void deleteItemFromCart(long productId, long userId);

  void updateItemAmount(CartItemDto cartItemDto, long userId);

  OrderDto checkout(long userId);

  void createNewCart(long userId);

  void clearCart(long userId);

  void addItem(CartItemDto cartItemDto, long userId);
}
