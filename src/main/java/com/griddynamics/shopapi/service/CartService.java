package com.griddynamics.shopapi.service;

import com.griddynamics.shopapi.dto.CartDto;
import com.griddynamics.shopapi.dto.OrderDto;
import com.griddynamics.shopapi.dto.OrderItemDto;
import com.griddynamics.shopapi.dto.SessionInfo;

public interface CartService {

  CartDto getCartFor(SessionInfo sessionInfo);

  CartDto getCartFor(long clientId);

  void deleteItemFromCart(long productId, SessionInfo sessionInfo);

  void updateItemAmount(OrderItemDto orderItemDto, SessionInfo sessionInfo);

  OrderDto checkout(SessionInfo sessionInfo);

  long getIdOfNewCart(SessionInfo sessionInfo);

  void clearCart(SessionInfo sessionInfo);

  void addItem(OrderItemDto orderItemDto, SessionInfo sessionInfo);
}
