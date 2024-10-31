package com.griddynamics.shopapi.service.impl;

import com.griddynamics.shopapi.dto.CartDto;
import com.griddynamics.shopapi.dto.OrderDto;
import com.griddynamics.shopapi.dto.OrderItemDto;
import com.griddynamics.shopapi.dto.SessionInfo;
import com.griddynamics.shopapi.exception.*;
import com.griddynamics.shopapi.model.*;
import com.griddynamics.shopapi.repository.OrderRepository;
import com.griddynamics.shopapi.repository.UserRepository;
import com.griddynamics.shopapi.service.CartService;
import com.griddynamics.shopapi.service.ProductService;
import jakarta.transaction.Transactional;
import java.util.Optional;
import org.springframework.stereotype.Service;

@Service
@Transactional
public class CartServiceImpl implements CartService {

  private final OrderRepository orderRepository;
  private final UserRepository userRepository;
  private final ProductService productService;

  public CartServiceImpl(
      OrderRepository orderRepository,
      UserRepository userRepository,
      ProductService productService) {
    this.orderRepository = orderRepository;
    this.userRepository = userRepository;
    this.productService = productService;
  }

  @Override
  public CartDto getCartFor(SessionInfo sessionInfo) {
    validateSessionInfo(sessionInfo);
    OrderDetails cart = getCartFromDb(sessionInfo.getCartId());
    return new CartDto(cart);
  }

  @Override
  public CartDto getCartFor(long userId) {
    Optional<OrderDetails> cartFromDb = orderRepository.findCartByUserId(userId);
    if (cartFromDb.isEmpty() || cartFromDb.get().getStatus().equals(OrderStatus.CART)) {
      throw new CartNotFoundException(
          String.format("Cart not found for a user with id %d", userId));
    }
    return new CartDto(cartFromDb.get());
  }

  @Override
  public void deleteItemFromCart(long productId, SessionInfo sessionInfo) {
    validateSessionInfo(sessionInfo);
    OrderDetails cart = getCartFromDb(sessionInfo.getCartId());
    cart.removeProduct(productId);
    orderRepository.save(cart);
  }

  @Override
  public void updateItemAmount(OrderItemDto orderItemDto, SessionInfo sessionInfo) {
    validateSessionInfo(sessionInfo);
    if (!productService.isAvailableProductWithAmount(
        orderItemDto.getProductId(), orderItemDto.getQuantity())) {
      throw new ProductNotAvailableException(
          String.format(
              "There is no %1$d units of product with id %2$d",
              orderItemDto.getQuantity(), orderItemDto.getProductId()));
    }

    OrderDetails cart = getCartFromDb(sessionInfo.getCartId());
    fillOrderItemInfo(orderItemDto, cart);
    cart.updateProductAmount(orderItemDto.getProductId(), orderItemDto.getQuantity());

    orderRepository.save(cart);
  }

  @Override
  public OrderDto checkout(SessionInfo sessionInfo) {
    validateSessionInfo(sessionInfo);
    OrderDetails cart = getCartFromDb(sessionInfo.getCartId());
    if (cart.getTotal().doubleValue() <= 0) {
      throw new ConversionException("Empty cart cannot be convert to ordered order");
    }
    productService.validateAndUpdatePricesForOrder(cart);
    productService.updateAvailabilityOfProductsIn(cart);

    cart.setStatus(OrderStatus.ORDERED);
    orderRepository.save(cart);
    return new OrderDto(cart);
  }

  @Override
  public long getIdOfNewCart(SessionInfo sessionInfo) {
    User user = getClientFromDb(sessionInfo.getUserId());
    Optional<OrderDetails> existingCart = orderRepository.findCartByUserId(user.getId());
    if (existingCart.isPresent()) {
      throw new ForbiddenResourcesException(
          String.format("Cart for user with id %d already exists", user.getId()));
    }

    OrderDetails newCart = new OrderDetails();
    newCart.setStatus(OrderStatus.CART);
    newCart.setUser(user);
    OrderDetails savedCart = orderRepository.save(newCart);

    return savedCart.getId();
  }

  private User getClientFromDb(long userId) {
    Optional<User> userFromDb = userRepository.findById(userId);
    if (userFromDb.isEmpty()) {
      throw new UserNotFoundException(String.format("user with id %d not found", userId));
    }
    return userFromDb.get();
  }

  @Override
  public void clearCart(SessionInfo sessionInfo) {
    validateSessionInfo(sessionInfo);
    OrderDetails cart = getCartFromDb(sessionInfo.getCartId());
    cart.clearOrder();
    orderRepository.save(cart);
  }

  @Override
  public void addItem(OrderItemDto orderItemDto, SessionInfo sessionInfo) {
    validateSessionInfo(sessionInfo);
    if (!productService.isAvailableProductWithAmount(
        orderItemDto.getProductId(), orderItemDto.getQuantity())) {
      throw new ProductNotAvailableException(
          String.format(
              "There is no %1$d units of product with id %2$d",
              orderItemDto.getQuantity(), orderItemDto.getProductId()));
    }

    OrderDetails cart = getCartFromDb(sessionInfo.getCartId());
    fillOrderItemInfo(orderItemDto, cart);
    cart.addProduct(
        productService.getProductById(orderItemDto.getProductId()), orderItemDto.getQuantity());

    orderRepository.save(cart);
  }

  private OrderDetails getCartFromDb(long cartId) {
    Optional<OrderDetails> cartFromDb = orderRepository.findById(cartId);
    if (cartFromDb.isEmpty() || !cartFromDb.get().getStatus().equals(OrderStatus.CART)) {
      throw new CartNotFoundException(String.format("Cart with id %d not found", cartId));
    }
    return cartFromDb.get();
  }

  private void validateSessionInfo(SessionInfo sessionInfo) {
    Optional<Long> userIdFromCart =
        orderRepository.findUserIdByIdAndStatusIsCart(sessionInfo.getCartId());
    if (userIdFromCart.isEmpty()) {
      throw new CartNotFoundException(
          String.format("Cart with id %d not found", sessionInfo.getCartId()));
    }
    if (!userIdFromCart.get().equals(sessionInfo.getUserId())) {
      throw new ForbiddenResourcesException(
          String.format(
              "userId %1$d doesn't match info for cart on the id %2$d",
              sessionInfo.getUserId(), sessionInfo.getCartId()));
    }
  }

  private void fillOrderItemInfo(OrderItemDto orderItemDto, OrderDetails cart) {
    if (orderItemDto.getProductId() == null) {

      if (orderItemDto.getId() >= cart.getItems().size()) {
        throw new ConversionException(
            String.format(
                "Wrong id of the item. Cart doesn't have order item with id %d",
                orderItemDto.getId()));
      }

      orderItemDto.setProductId(cart.getItems().get(orderItemDto.getId()).getProductId());
    }
  }
}
