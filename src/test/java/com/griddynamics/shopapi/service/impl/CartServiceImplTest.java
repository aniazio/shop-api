package com.griddynamics.shopapi.service.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.when;

import com.griddynamics.shopapi.dto.CartDto;
import com.griddynamics.shopapi.dto.OrderDto;
import com.griddynamics.shopapi.dto.OrderItemDto;
import com.griddynamics.shopapi.dto.SessionInfo;
import com.griddynamics.shopapi.exception.CartNotFoundException;
import com.griddynamics.shopapi.exception.ConversionException;
import com.griddynamics.shopapi.exception.ForbiddenResourcesException;
import com.griddynamics.shopapi.exception.ProductNotAvailableException;
import com.griddynamics.shopapi.model.*;
import com.griddynamics.shopapi.repository.OrderRepository;
import com.griddynamics.shopapi.repository.UserRepository;
import com.griddynamics.shopapi.service.ProductService;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CartServiceImplTest {

  @Mock OrderRepository orderRepository;
  @Mock UserRepository userRepository;
  @Mock ProductService productService;
  @InjectMocks CartServiceImpl cartService;
  @Captor ArgumentCaptor<OrderDetails> captor;
  SessionInfo sessionInfo;
  String sessionId = "dsfs-12x-sfd-ads";
  long userId = 2312L;
  long cartId = 342L;
  long productId = 897L;
  BigDecimal item1Price = BigDecimal.valueOf(10.03);
  OrderDetails cart;
  User user;
  double secondItemTotal;

  @BeforeEach
  void setUp() {
    sessionInfo = new SessionInfo(sessionId, userId, cartId);
    cart = new OrderDetails();
    cart.setStatus(OrderStatus.CART);
    cart.setId(cartId);
    cart.setCreatedAt(LocalDateTime.now());

    user = new User();
    user.setId(userId);
    cart.setUser(user);

    ArrayList<OrderItem> items = new ArrayList<>();
    OrderItem item1 = new OrderItem();
    item1.setOrder(cart);
    item1.setQuantity(10);
    item1.setPrice(item1Price);
    Product product = new Product();
    product.setId(productId);
    item1.setProduct(product);
    items.add(item1);

    OrderItem item2 = new OrderItem();
    item2.setOrder(cart);
    item2.setQuantity(8);
    item2.setPrice(BigDecimal.valueOf(3.7));
    Product product2 = new Product();
    product2.setId(98L);
    item2.setProduct(product2);
    items.add(item2);
    secondItemTotal = BigDecimal.valueOf(3.7).multiply(BigDecimal.valueOf(8)).doubleValue();

    cart.setItems(items);
    cart.setTotal(
        item1Price.multiply(BigDecimal.valueOf(10)).add(BigDecimal.valueOf(secondItemTotal)));
  }

  @Test
  void should_getCartFor_properRequest() {
    when(orderRepository.findUserIdByIdAndStatusIsCart(cartId)).thenReturn(Optional.of(userId));

    when(orderRepository.findById(cartId)).thenReturn(Optional.of(cart));

    CartDto returned = cartService.getCartFor(sessionInfo);

    assertEquals(cartId, returned.getId());
    assertEquals(userId, returned.getUserId());
    assertEquals(cart.getTotal().doubleValue(), returned.getTotal());
    assertEquals(cart.getItems().size(), returned.getItems().size());
  }

  @Test
  void should_deleteItemFromCart_properRequest() {
    when(orderRepository.findUserIdByIdAndStatusIsCart(cartId)).thenReturn(Optional.of(userId));

    when(orderRepository.findById(cartId)).thenReturn(Optional.of(cart));
    when(orderRepository.save(captor.capture())).thenReturn(null);

    cartService.deleteItemFromCart(productId, sessionInfo);

    OrderDetails captured = captor.getValue();

    assertEquals(1, captured.getItems().size());
    assertEquals(secondItemTotal, captured.getTotal().doubleValue());

    assertEquals(cart.getStatus(), captured.getStatus());
    assertEquals(cartId, captured.getId());
    assertEquals(userId, captured.getUser().getId());
    assertEquals(cart.getCreatedAt(), captured.getCreatedAt());
  }

  @Test
  void should_updateItemAmount_when_available() {
    when(orderRepository.findUserIdByIdAndStatusIsCart(cartId)).thenReturn(Optional.of(userId));

    OrderItemDto itemDto = new OrderItemDto();
    itemDto.setProductId(productId);
    itemDto.setQuantity(5);

    when(productService.isAvailableProductWithAmount(productId, itemDto.getQuantity()))
        .thenReturn(true);
    when(orderRepository.findById(cartId)).thenReturn(Optional.of(cart));
    when(orderRepository.save(captor.capture())).thenReturn(null);

    cartService.updateItemAmount(itemDto, sessionInfo);

    OrderDetails captured = captor.getValue();

    assertEquals(
        item1Price.multiply(BigDecimal.valueOf(5)).doubleValue() + 3.7 * 8,
        captured.getTotal().doubleValue());
    assertEquals(2, captured.getItems().size());
    assertEquals(5, captured.getItems().get(0).getQuantity());
    assertEquals(item1Price, captured.getItems().get(0).getPrice());
  }

  @Test
  void shouldNot_updateItemAmount_when_notAvailable() {
    when(orderRepository.findUserIdByIdAndStatusIsCart(cartId)).thenReturn(Optional.of(userId));

    OrderItemDto itemDto = new OrderItemDto();
    itemDto.setProductId(productId);
    itemDto.setQuantity(5);

    when(productService.isAvailableProductWithAmount(productId, itemDto.getQuantity()))
        .thenReturn(false);

    assertThrows(
        ProductNotAvailableException.class,
        () -> {
          cartService.updateItemAmount(itemDto, sessionInfo);
        });
  }

  @Test
  void should_checkout_when_positiveTotal() {
    when(orderRepository.findUserIdByIdAndStatusIsCart(cartId)).thenReturn(Optional.of(userId));

    when(orderRepository.findById(cartId)).thenReturn(Optional.of(cart));
    when(orderRepository.save(captor.capture())).thenReturn(null);

    OrderDto returned = cartService.checkout(sessionInfo);

    OrderDetails captured = captor.getValue();

    assertEquals(OrderStatus.ORDERED, returned.getStatus());
    assertEquals(OrderStatus.ORDERED, captured.getStatus());
    assertEquals(cartId, returned.getId());
    assertEquals(userId, returned.getUserId());
    assertEquals(cart.getItems().size(), returned.getItems().size());
    assertEquals(cart.getTotal().doubleValue(), returned.getTotal());

    then(productService).should().validateAndUpdatePricesForOrder(cart);
    then(productService).should().updateAvailabilityForProductsIn(cart);
  }

  @Test
  void shouldNot_checkout_when_zeroTotal() {
    when(orderRepository.findUserIdByIdAndStatusIsCart(cartId)).thenReturn(Optional.of(userId));

    cart.setTotal(BigDecimal.ZERO);
    cart.setItems(new ArrayList<>());
    when(orderRepository.findById(cartId)).thenReturn(Optional.of(cart));

    assertThrows(
        ConversionException.class,
        () -> {
          cartService.checkout(sessionInfo);
        });

    then(productService).shouldHaveNoInteractions();
  }

  @Test
  void shouldNot_checkout_when_alreadySubmitted() {
    when(orderRepository.findUserIdByIdAndStatusIsCart(cartId)).thenReturn(Optional.of(userId));

    cart.setStatus(OrderStatus.ORDERED);
    when(orderRepository.findById(cartId)).thenReturn(Optional.of(cart));

    assertThrows(
        CartNotFoundException.class,
        () -> {
          cartService.checkout(sessionInfo);
        });

    then(productService).shouldHaveNoInteractions();
  }

  @Test
  void should_getIdOfNewCart_when_noCartForThisUserInDb() {
    when(userRepository.findById(userId)).thenReturn(Optional.of(user));
    when(orderRepository.findCartByUserId(userId)).thenReturn(Optional.empty());
    when(orderRepository.save(captor.capture())).thenReturn(cart);

    long returned = cartService.getIdOfNewCart(sessionInfo);

    OrderDetails captured = captor.getValue();

    assertEquals(cartId, returned);
    assertEquals(OrderStatus.CART, captured.getStatus());
    assertEquals(userId, captured.getUser().getId());
    assertEquals(0, captured.getTotal().doubleValue());
    assertEquals(0, captured.getItems().size());
  }

  @Test
  void shouldNot_getIdOfNewCart_when_cartForThisUserInDb() {
    when(userRepository.findById(userId)).thenReturn(Optional.of(user));
    when(orderRepository.findCartByUserId(userId)).thenReturn(Optional.of(cart));

    assertThrows(
        ForbiddenResourcesException.class,
        () -> {
          cartService.getIdOfNewCart(sessionInfo);
        });
  }

  @Test
  void should_clearCart_properRequest() {
    when(orderRepository.findUserIdByIdAndStatusIsCart(cartId)).thenReturn(Optional.of(userId));
    when(orderRepository.findById(cartId)).thenReturn(Optional.of(cart));
    when(orderRepository.save(captor.capture())).thenReturn(null);

    cartService.clearCart(sessionInfo);

    OrderDetails captured = captor.getValue();

    assertEquals(0, captured.getItems().size());
    assertEquals(0, captured.getTotal().doubleValue());
  }

  @Test
  void should_addItem_when_newItem() {
    when(orderRepository.findUserIdByIdAndStatusIsCart(cartId)).thenReturn(Optional.of(userId));

    OrderItemDto itemDto = new OrderItemDto();
    long addedProductId = 12L;
    itemDto.setProductId(addedProductId);
    itemDto.setQuantity(5);
    Product addedProduct = new Product();
    addedProduct.setPrice(BigDecimal.valueOf(7));
    addedProduct.setId(addedProductId);

    when(productService.isAvailableProductWithAmount(itemDto.getProductId(), itemDto.getQuantity()))
        .thenReturn(true);
    when(orderRepository.findById(cartId)).thenReturn(Optional.of(cart));
    when(orderRepository.save(captor.capture())).thenReturn(null);
    when(productService.getProductById(addedProductId)).thenReturn(addedProduct);

    cartService.addItem(itemDto, sessionInfo);

    OrderDetails captured = captor.getValue();

    assertEquals(
        item1Price.doubleValue() * 10 + secondItemTotal + 5 * 7, captured.getTotal().doubleValue());
    assertEquals(3, captured.getItems().size());
    assertEquals(5, captured.getItems().get(2).getQuantity());
    assertEquals(addedProductId, captured.getItems().get(2).getProductId());
  }

  @Test
  void should_addItem_when_itemAlreadyInCart() {
    when(orderRepository.findUserIdByIdAndStatusIsCart(cartId)).thenReturn(Optional.of(userId));

    OrderItemDto itemDto = new OrderItemDto();
    itemDto.setProductId(productId);
    itemDto.setQuantity(5);
    Product product = new Product();
    product.setId(productId);
    product.setPrice(item1Price);

    when(productService.isAvailableProductWithAmount(itemDto.getProductId(), itemDto.getQuantity()))
        .thenReturn(true);
    when(orderRepository.findById(cartId)).thenReturn(Optional.of(cart));
    when(orderRepository.save(captor.capture())).thenReturn(null);
    when(productService.getProductById(productId)).thenReturn(product);

    cartService.addItem(itemDto, sessionInfo);

    OrderDetails captured = captor.getValue();

    assertEquals(
        item1Price
            .multiply(BigDecimal.valueOf(15))
            .add(BigDecimal.valueOf(secondItemTotal))
            .doubleValue(),
        captured.getTotal().doubleValue());
    assertEquals(2, captured.getItems().size());
    assertEquals(15, captured.getItems().get(0).getQuantity());
    assertEquals(productId, captured.getItems().get(0).getProductId());
  }
}
