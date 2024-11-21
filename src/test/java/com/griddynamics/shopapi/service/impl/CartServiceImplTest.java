package com.griddynamics.shopapi.service.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.when;

import com.griddynamics.shopapi.dto.*;
import com.griddynamics.shopapi.exception.ConversionException;
import com.griddynamics.shopapi.exception.ForbiddenResourcesException;
import com.griddynamics.shopapi.exception.ProductNotAvailableException;
import com.griddynamics.shopapi.model.*;
import com.griddynamics.shopapi.repository.CartRepository;
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
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CartServiceImplTest {

  @Mock CartRepository cartRepository;
  @Mock OrderRepository orderRepository;
  @Mock UserRepository userRepository;
  @Mock ProductService productService;
  CartServiceImpl cartService;
  @Captor ArgumentCaptor<Cart> captor;
  long userId = 2312L;
  long productId = 897L;
  BigDecimal item1Price = BigDecimal.valueOf(10.03);
  int firstItemQuantity = 10;
  Cart cart;
  User user;
  double secondItemTotal;

  @BeforeEach
  void setUp() {
    cartService =
        new CartServiceImpl(cartRepository, orderRepository, userRepository, productService);
    cart = new Cart();
    cart.setCreatedAt(LocalDateTime.now());

    user = new User();
    user.setId(userId);
    cart.setUser(user);

    ArrayList<CartItem> items = new ArrayList<>();
    CartItem item1 = new CartItem();
    item1.setCart(cart);
    item1.setQuantity(firstItemQuantity);
    item1.setPrice(item1Price);
    Product product = new Product();
    product.setId(productId);
    item1.setProduct(product);
    items.add(item1);

    CartItem item2 = new CartItem();
    item2.setCart(cart);
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
    when(cartRepository.findByUserId(userId)).thenReturn(Optional.of(cart));

    CartDto returned = cartService.getCartFor(userId);

    assertEquals(userId, returned.getUserId());
    assertEquals(cart.getTotal().doubleValue(), returned.getTotal());
    assertEquals(cart.getItems().size(), returned.getItems().size());
  }

  @Test
  void should_deleteItemFromCart_properRequest() {
    when(cartRepository.findByUserId(userId)).thenReturn(Optional.of(cart));
    when(cartRepository.save(captor.capture())).thenReturn(null);

    cartService.deleteItemFromCart(productId, userId);

    Cart captured = captor.getValue();

    assertEquals(1, captured.getItems().size());
    assertEquals(secondItemTotal, captured.getTotal().doubleValue());
    assertEquals(userId, captured.getUser().getId());
    assertEquals(cart.getCreatedAt(), captured.getCreatedAt());
  }

  @Test
  void should_updateItemAmount_when_available() {
    CartItemDto itemDto = new CartItemDto();
    itemDto.setProductId(productId);
    itemDto.setQuantity(5);

    when(productService.isAvailableProductWithAmount(productId, itemDto.getQuantity()))
        .thenReturn(true);
    when(cartRepository.findByUserId(userId)).thenReturn(Optional.of(cart));
    when(cartRepository.save(captor.capture())).thenReturn(null);

    cartService.updateItemAmount(itemDto, userId);

    Cart captured = captor.getValue();

    assertEquals(
        item1Price.multiply(BigDecimal.valueOf(5)).doubleValue() + 3.7 * 8,
        captured.getTotal().doubleValue());
    assertEquals(2, captured.getItems().size());
    assertEquals(5, captured.getItems().get(0).getQuantity());
    assertEquals(item1Price, captured.getItems().get(0).getPrice());
  }

  @Test
  void shouldNot_updateItemAmount_when_notAvailable() {
    CartItemDto itemDto = new CartItemDto();
    itemDto.setProductId(productId);
    itemDto.setQuantity(5);

    when(productService.isAvailableProductWithAmount(productId, itemDto.getQuantity()))
        .thenReturn(false);

    assertThrows(
        ProductNotAvailableException.class,
        () -> {
          cartService.updateItemAmount(itemDto, userId);
        });
  }

  @Test
  void should_checkout_when_positiveTotal() {
    when(cartRepository.findByUserId(userId)).thenReturn(Optional.of(cart));
    ArgumentCaptor<OrderDetails> orderCaptor = ArgumentCaptor.forClass(OrderDetails.class);
    OrderDetails savedOrder = new OrderDetails(cart);
    savedOrder.setId(3224L);
    savedOrder.setStatus(OrderStatus.ORDERED);
    when(orderRepository.save(orderCaptor.capture())).thenReturn(savedOrder);

    OrderDto returned = cartService.checkout(userId);

    OrderDetails captured = orderCaptor.getValue();

    assertEquals(OrderStatus.ORDERED, captured.getStatus());
    assertEquals(userId, returned.getUserId());
    assertEquals(cart.getTotal().doubleValue(), returned.getTotal());

    then(productService).should().validateAndUpdatePricesForCart(cart);
    then(productService).should().updateAvailabilityForProductsIn(cart);
  }

  @Test
  void shouldNot_checkout_when_zeroTotal() {
    cart.setTotal(BigDecimal.ZERO);
    cart.setItems(new ArrayList<>());
    when(cartRepository.findByUserId(userId)).thenReturn(Optional.of(cart));

    assertThrows(
        ConversionException.class,
        () -> {
          cartService.checkout(userId);
        });

    then(productService).shouldHaveNoInteractions();
  }

  @Test
  void should_getIdOfNewCart_when_noCartForThisUserInDb() {
    when(userRepository.findById(userId)).thenReturn(Optional.of(user));
    when(cartRepository.findByUserId(userId)).thenReturn(Optional.empty());
    when(cartRepository.save(captor.capture())).thenReturn(cart);

    cartService.createNewCart(userId);

    Cart captured = captor.getValue();

    assertEquals(userId, captured.getUser().getId());
    assertEquals(0, captured.getTotal().doubleValue());
    assertEquals(0, captured.getItems().size());
  }

  @Test
  void shouldNot_getIdOfNewCart_when_cartForThisUserInDb() {
    when(userRepository.findById(userId)).thenReturn(Optional.of(user));
    when(cartRepository.findByUserId(userId)).thenReturn(Optional.of(cart));

    assertThrows(
        ForbiddenResourcesException.class,
        () -> {
          cartService.createNewCart(userId);
        });
  }

  @Test
  void should_clearCart_properRequest() {
    when(cartRepository.findByUserId(userId)).thenReturn(Optional.of(cart));
    when(cartRepository.save(captor.capture())).thenReturn(null);

    cartService.clearCart(userId);

    Cart captured = captor.getValue();

    assertEquals(0, captured.getItems().size());
    assertEquals(0, captured.getTotal().doubleValue());
  }

  @Test
  void should_addItem_when_newItem() {
    CartItemDto itemDto = new CartItemDto();
    long addedProductId = 12L;
    itemDto.setProductId(addedProductId);
    itemDto.setQuantity(5);
    Product addedProduct = new Product();
    addedProduct.setPrice(BigDecimal.valueOf(7));
    addedProduct.setId(addedProductId);

    when(productService.isAvailableProductWithAmount(itemDto.getProductId(), itemDto.getQuantity()))
        .thenReturn(true);
    when(cartRepository.findByUserId(userId)).thenReturn(Optional.of(cart));
    when(cartRepository.save(captor.capture())).thenReturn(null);
    when(productService.getProductById(addedProductId)).thenReturn(addedProduct);

    cartService.addItem(itemDto, userId);

    Cart captured = captor.getValue();

    assertEquals(
        item1Price.doubleValue() * 10 + secondItemTotal + 5 * 7, captured.getTotal().doubleValue());
    assertEquals(3, captured.getItems().size());
    assertEquals(5, captured.getItems().get(2).getQuantity());
    assertEquals(addedProductId, captured.getItems().get(2).getProductId());
  }

  @Test
  void should_addItem_when_itemAlreadyInCart() {
    CartItemDto itemDto = new CartItemDto();
    itemDto.setProductId(productId);
    itemDto.setQuantity(5);
    Product product = new Product();
    product.setId(productId);
    product.setPrice(item1Price);

    when(productService.isAvailableProductWithAmount(
            itemDto.getProductId(), itemDto.getQuantity() + firstItemQuantity))
        .thenReturn(true);
    when(cartRepository.findByUserId(userId)).thenReturn(Optional.of(cart));
    when(cartRepository.save(captor.capture())).thenReturn(null);
    when(productService.getProductById(productId)).thenReturn(product);

    cartService.addItem(itemDto, userId);

    Cart captured = captor.getValue();

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
