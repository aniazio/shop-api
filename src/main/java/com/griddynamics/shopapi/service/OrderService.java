package com.griddynamics.shopapi.service;

import com.griddynamics.shopapi.dto.OrderDto;
import java.util.List;

public interface OrderService {

  List<OrderDto> getAllOrderForUser(long userId);

  OrderDto getOrderForUser(long userId, long orderId);

  void cancelOrder(long userId, long orderId);
}
