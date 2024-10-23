package com.griddynamics.shopapi.service;

import com.griddynamics.shopapi.dto.OrderDto;
import com.griddynamics.shopapi.dto.OrderListDto;

public interface OrderService {

  OrderListDto getAllOrderFor(long userId);

  OrderDto getOrderFor(long userId, long orderId);

  void deleteOrder(long userId, long orderId);
}
