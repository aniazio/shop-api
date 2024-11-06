package com.griddynamics.shopapi.service.impl;

import com.griddynamics.shopapi.dto.OrderDto;
import com.griddynamics.shopapi.exception.ConversionException;
import com.griddynamics.shopapi.exception.OrderNotFoundException;
import com.griddynamics.shopapi.model.OrderDetails;
import com.griddynamics.shopapi.model.OrderStatus;
import com.griddynamics.shopapi.repository.OrderRepository;
import com.griddynamics.shopapi.service.OrderService;
import com.griddynamics.shopapi.service.ProductService;
import jakarta.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;
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
  public List<OrderDto> getAllOrderForUser(long userId) {
    Set<OrderDetails> orders = orderRepository.findByUserId(userId);
    List<OrderDto> ordersDto = new ArrayList<>();
    orders.stream()
        .filter(order -> order.getStatus() != OrderStatus.CART)
        .forEach(order -> ordersDto.add(new OrderDto(order)));
    return ordersDto;
  }

  @Override
  public OrderDto getOrderForUser(long userId, long orderId) {
    OrderDetails order = getOrderDetailsFromDb(userId, orderId);
    return new OrderDto(order);
  }

  @Override
  public void cancelOrder(long userId, long orderId) {
    OrderDetails order = getOrderDetailsFromDb(userId, orderId);
    if (!order.getStatus().equals(OrderStatus.ORDERED)) {
      throw new ConversionException(
          String.format("Order with status %s cannot be canceled", order.getStatus()));
    }
    productService.addItemsToAvailable(order.getItems());
    order.setStatus(OrderStatus.CANCELED);
    orderRepository.save(order);
  }

  private OrderDetails getOrderDetailsFromDb(long userId, long orderId) {
    Optional<OrderDetails> orderFromDb = orderRepository.findByIdAndUserId(orderId, userId);
    if (orderFromDb.isEmpty()) {
      throw new OrderNotFoundException(
          String.format("Order with id %1$d not found for the user %2$d", orderId, userId));
    }
    return orderFromDb.get();
  }
}
