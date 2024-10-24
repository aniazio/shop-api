package com.griddynamics.shopapi.service;

import com.griddynamics.shopapi.dto.OrderDto;
import com.griddynamics.shopapi.dto.OrderListDto;
import com.griddynamics.shopapi.exception.OrderNotFoundException;
import com.griddynamics.shopapi.model.OrderDetails;
import com.griddynamics.shopapi.repository.OrderRepository;
import java.util.Optional;
import java.util.Set;
import org.springframework.stereotype.Service;

@Service
public class OrderServiceImpl implements OrderService {

  private final OrderRepository orderRepository;

  public OrderServiceImpl(OrderRepository orderRepository) {
    this.orderRepository = orderRepository;
  }

  @Override
  public OrderListDto getAllOrderFor(long clientId) {
    Set<OrderDetails> orders = orderRepository.findByClientId(clientId);
    OrderListDto ordersDto = new OrderListDto();
    orders.forEach(order -> ordersDto.addOrder(new OrderDto(order)));
    return ordersDto;
  }

  @Override
  public OrderDto getOrderFor(long userId, long orderId) {
    OrderDetails order = getOrderDetailsFromDb(userId, orderId);
    return new OrderDto(order);
  }

  @Override
  public void deleteOrder(long userId, long orderId) {
    OrderDetails order = getOrderDetailsFromDb(userId, orderId);
    orderRepository.delete(order);
  }

  public OrderDetails getOrderDetailsFromDb(long userId, long orderId) {
    Optional<OrderDetails> orderFromDb = orderRepository.findByIdAndClientId(orderId, userId);
    if (orderFromDb.isEmpty()) {
      throw new OrderNotFoundException(
          "Order with id " + orderId + " not found for the user " + userId);
    }
    return orderFromDb.get();
  }
}
