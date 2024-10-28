package com.griddynamics.shopapi.service.impl;

import com.griddynamics.shopapi.dto.CartDto;
import com.griddynamics.shopapi.dto.OrderDto;
import com.griddynamics.shopapi.dto.OrderItemDto;
import com.griddynamics.shopapi.dto.SessionInfo;
import com.griddynamics.shopapi.exception.*;
import com.griddynamics.shopapi.model.*;
import com.griddynamics.shopapi.repository.OrderRepository;
import com.griddynamics.shopapi.repository.ProductRepository;
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
  private final ProductRepository productRepository;
  private final UserRepository userRepository;
  private final ProductService productService;

  public CartServiceImpl(
      OrderRepository orderRepository,
      ProductRepository productRepository,
      UserRepository userRepository,
      ProductService productService) {
    this.orderRepository = orderRepository;
    this.productRepository = productRepository;
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
  public CartDto getCartFor(long clientId) {
    Optional<OrderDetails> cartFromDb = orderRepository.findCartByUserId(clientId);
    if (cartFromDb.isEmpty() || cartFromDb.get().getStatus().equals(OrderStatus.CART)) {
      throw new CartNotFoundException("Cart not found for user with id " + clientId);
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
    OrderDetails cart = getCartFromDb(sessionInfo.getCartId());

    int diff =
        cart.updateAndGetDifferenceInProductAmount(
            orderItemDto.getProductId(), orderItemDto.getQuantity());
    updateAvailabilityOfProduct(orderItemDto.getProductId(), diff);

    orderRepository.save(cart);
  }

  private void updateAvailabilityOfProduct(long productId, int diff) {
    Optional<Product> productFromDb = productRepository.findById(productId);
    if (productFromDb.isEmpty()) {
      throw new ProductNotFoundException("product with id " + productId + " not found");
    }

    Product product = productFromDb.get();
    if (product.getAvailable() < diff) {
      throw new ProductNotAvailableException(
          "Cannot update the product with id "
              + productId
              + " in the cart, because there are not enough units available");
    }

    product.setAvailable(product.getAvailable() - diff);
    productRepository.save(product);
  }

  @Override
  public OrderDto checkout(SessionInfo sessionInfo) {
    validateSessionInfo(sessionInfo);
    OrderDetails cart = getCartFromDb(sessionInfo.getCartId());
    if (cart.getTotal().doubleValue() <= 0) {
      throw new ConversionException("Empty cart cannot be convert to ordered order");
    }
    productService.validateAndUpdatePrices(cart);

    cart.setStatus(OrderStatus.ORDERED);
    orderRepository.save(cart);
    return new OrderDto(cart);
  }

  @Override
  public long getIdOfNewCart(SessionInfo sessionInfo) {
    User user = getClientFromDb(sessionInfo.getUserId());
    Optional<OrderDetails> existingCart = orderRepository.findCartByUserId(user.getId());
    if (existingCart.isPresent()) {
      throw new ForbiddenResourcesException("Cart for these user already exists");
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
      throw new UserNotFoundException("user with id " + userId + " not found");
    }
    return userFromDb.get();
  }

  @Override
  public void clearCart(SessionInfo sessionInfo) {
    validateSessionInfo(sessionInfo);
    OrderDetails cart = getCartFromDb(sessionInfo.getCartId());
    productService.resetAvailabilityForOrderClearing(cart.getItems());
    cart.clearOrder();
    orderRepository.save(cart);
  }

  @Override
  public void addItem(OrderItemDto orderItemDto, SessionInfo sessionInfo) {
    validateSessionInfo(sessionInfo);
    OrderDetails cart = getCartFromDb(sessionInfo.getCartId());

    int diff =
        cart.updateAndGetDifferenceInProductAmount(
            orderItemDto.getProductId(), orderItemDto.getQuantity());
    updateAvailabilityOfProduct(orderItemDto.getProductId(), -diff);
    Optional<Product> productFromDb = productRepository.findById(orderItemDto.getProductId());
    cart.addProduct(productFromDb.get(), orderItemDto.getQuantity());
    orderRepository.save(cart);
  }

  private OrderDetails getCartFromDb(long cartId) {
    Optional<OrderDetails> cartFromDb = orderRepository.findById(cartId);
    if (cartFromDb.isEmpty() || !cartFromDb.get().getStatus().equals(OrderStatus.CART)) {
      throw new CartNotFoundException("Cart with id " + cartId + " not found");
    }
    return cartFromDb.get();
  }

  private void validateSessionInfo(SessionInfo sessionInfo) {
    Optional<Long> userIdFromCart =
        orderRepository.findUserIdByIdAndStatusIsCart(sessionInfo.getCartId());
    if (userIdFromCart.isEmpty()) {
      throw new CartNotFoundException("cart with id " + sessionInfo.getCartId() + " not found");
    }
    if (!userIdFromCart.get().equals(sessionInfo.getUserId())) {
      throw new ForbiddenResourcesException(
          "userId "
              + sessionInfo.getUserId()
              + " doesn't match info for cart of the id "
              + sessionInfo.getCartId());
    }
  }
}
