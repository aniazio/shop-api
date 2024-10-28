package com.griddynamics.shopapi.service.impl;

import com.griddynamics.shopapi.dto.OrderDto;
import com.griddynamics.shopapi.dto.OrderListDto;
import com.griddynamics.shopapi.exception.OrderNotFoundException;
import com.griddynamics.shopapi.model.OrderDetails;
import com.griddynamics.shopapi.model.OrderStatus;
import com.griddynamics.shopapi.repository.OrderRepository;
import com.griddynamics.shopapi.service.OrderService;
import com.griddynamics.shopapi.service.ProductService;
import jakarta.transaction.Transactional;
import java.util.Optional;
import java.util.Set;
import org.springframework.stereotype.Service;

@Service
@Transactional
public class OrderServiceImpl implements OrderService {

  private final OrderRepository orderRepository;
  private final ProductService productService;

  public OrderServiceImpl(OrderRepository orderRepository, ProductService productService) {
    this.orderRepository = orderRepository;
    this.productService = productService;
  }

  @Override
  public OrderListDto getAllOrderFor(long userId) {
    Set<OrderDetails> orders = orderRepository.findByUserId(userId);
    OrderListDto ordersDto = new OrderListDto();
    orders.stream()
        .filter(order -> order.getStatus() != OrderStatus.CART)
        .forEach(order -> ordersDto.addOrder(new OrderDto(order)));
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
    productService.resetAvailabilityForOrderClearing(order.getItems());
    order.setStatus(OrderStatus.CANCELED);
    orderRepository.save(order);
  }

  public OrderDetails getOrderDetailsFromDb(long userId, long orderId) {
    Optional<OrderDetails> orderFromDb = orderRepository.findByIdAndUserId(orderId, userId);
    if (orderFromDb.isEmpty()) {
      throw new OrderNotFoundException(
          "Order with id " + orderId + " not found for the user " + userId);
    }
    return orderFromDb.get();
  }
}
