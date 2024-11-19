package com.griddynamics.shopapi.service.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.when;

import com.griddynamics.shopapi.dto.OrderDto;
import com.griddynamics.shopapi.exception.ConversionException;
import com.griddynamics.shopapi.exception.OrderNotFoundException;
import com.griddynamics.shopapi.model.*;
import com.griddynamics.shopapi.repository.OrderRepository;
import com.griddynamics.shopapi.service.ProductService;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class OrderServiceImplTest {

  @Mock OrderRepository orderRepository;
  @Mock ProductService productService;
  @InjectMocks OrderServiceImpl orderService;
  User user;
  OrderDetails order1, order2;
  long userId, order1Id, order2Id;
  Product product;
  int quantity;

  @BeforeEach
  void setUp() {
    user = new User();
    user.setId(234L);

    order1 = new OrderDetails();
    order1.setStatus(OrderStatus.ORDERED);
    order1.setId(2314L);
    order1.setCreatedAt(LocalDateTime.of(2020, 12, 12, 10, 0));
    order1.setUser(user);

    product = new Product();
    product.setId(234L);
    product.setPrice(BigDecimal.valueOf(10.3));
    quantity = 10;
    order1.addProduct(product, quantity);

    order2 = new OrderDetails();
    order2.setStatus(OrderStatus.CANCELED);
    order2.setId(654L);
    order2.setCreatedAt(LocalDateTime.of(2021, 10, 10, 8, 11));
    order2.setUser(user);

    Set<OrderDetails> orders = new HashSet<>();
    orders.add(order1);
    orders.add(order2);

    user.setOrders(orders);
    userId = user.getId();
    order1Id = order1.getId();
    order2Id = order2.getId();
  }

  @Test
  void should_getAllOrderForUser() {
    when(orderRepository.findByUserId(userId)).thenReturn(user.getOrders());

    List<OrderDto> returned = orderService.getAllOrderForUser(userId);

    assertEquals(2, returned.size());
    assertTrue(returned.stream().anyMatch(order -> order.getId() == order1Id));
    assertTrue(returned.stream().anyMatch(order -> order.getId() == order2Id));
  }

  @Test
  void should_getOrderForUser_orderInDb() {
    when(orderRepository.findByIdAndUserId(order1Id, userId)).thenReturn(Optional.of(order1));

    OrderDto returned = orderService.getOrderForUser(userId, order1Id);

    assertEquals(order1Id, returned.getId());
    assertEquals(userId, returned.getUserId());
    assertEquals(order1.getStatus(), returned.getStatus());
    assertEquals(
        product.getPrice().multiply(BigDecimal.valueOf(quantity)).doubleValue(),
        returned.getTotal());
    assertEquals(1, returned.getItems().size());
    assertEquals(product.getId(), returned.getItems().get(0).getProductId());
  }

  @Test
  void shouldNot_getOrderForUser_orderNotInDb() {
    when(orderRepository.findByIdAndUserId(order1Id, userId)).thenReturn(Optional.empty());

    assertThrows(
        OrderNotFoundException.class, () -> orderService.getOrderForUser(userId, order1Id));
  }

  @Test
  void should_changeStatusOfOrder_when_cancelOrder() {
    ArgumentCaptor<OrderDetails> captor = ArgumentCaptor.forClass(OrderDetails.class);
    when(orderRepository.findByIdAndUserId(order1Id, userId)).thenReturn(Optional.of(order1));
    when(orderRepository.save(captor.capture())).thenReturn(null);

    orderService.cancelOrder(userId, order1Id);

    OrderDetails captured = captor.getValue();

    assertEquals(OrderStatus.CANCELED, captured.getStatus());
    assertEquals(order1Id, captured.getId());

    then(productService).should().addItemsToAvailable(order1.getItems());
  }

  @Test
  void shouldNot_cancelOrder_when_alreadyCanceled() {
    when(orderRepository.findByIdAndUserId(order2Id, userId)).thenReturn(Optional.of(order2));

    assertThrows(ConversionException.class, () -> orderService.cancelOrder(userId, order2Id));

    then(productService).shouldHaveNoInteractions();
  }

}
