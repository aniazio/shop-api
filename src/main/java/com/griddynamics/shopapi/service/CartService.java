package com.griddynamics.shopapi.service;

import com.griddynamics.shopapi.dto.*;

public interface CartService {

  CartDto getCartFor(SessionInfo sessionInfo);

  void deleteItemFromCart(long productId, SessionInfo sessionInfo);

  void updateItemAmount(CartItemDto cartItemDto, SessionInfo sessionInfo);

  OrderDto checkout(SessionInfo sessionInfo);

  void createNewCart(SessionInfo sessionInfo);

  void clearCart(SessionInfo sessionInfo);

  void addItem(CartItemDto cartItemDto, SessionInfo sessionInfo);
}
