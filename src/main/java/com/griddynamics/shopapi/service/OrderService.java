package com.griddynamics.shopapi.service;

import com.griddynamics.shopapi.dto.OrderDto;
import java.util.List;

public interface OrderService {

  List<OrderDto> getAllOrderFor(long clientId);

  OrderDto getOrderFor(long userId, long orderId);

  void deleteOrder(long userId, long orderId);
}
