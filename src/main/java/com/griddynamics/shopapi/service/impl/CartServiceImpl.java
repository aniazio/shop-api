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
import com.griddynamics.shopapi.service.SessionService;
import jakarta.transaction.Transactional;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@Transactional
@RequiredArgsConstructor
public class CartServiceImpl implements CartService {

  private final OrderRepository orderRepository;
  private final UserRepository userRepository;
  private final ProductService productService;
  private final SessionService sessionService;

  @Override
  public CartDto getCartFor(SessionInfo sessionInfo) {
    sessionService.validateSessionInfo(sessionInfo);
    OrderDetails cart = getCartFromDb(sessionInfo.getCartId());
    return new CartDto(cart);
  }

  @Override
  public void deleteItemFromCart(long productId, SessionInfo sessionInfo) {
    sessionService.validateSessionInfo(sessionInfo);
    OrderDetails cart = getCartFromDb(sessionInfo.getCartId());
    cart.removeProduct(productId);
    orderRepository.save(cart);
  }

  @Override
  public void updateItemAmount(OrderItemDto orderItemDto, SessionInfo sessionInfo) {
    sessionService.validateSessionInfo(sessionInfo);
    int newAmount = orderItemDto.getQuantity();
    if (!productService.isAvailableProductWithAmount(orderItemDto.getProductId(), newAmount)) {
      throw new ProductNotAvailableException(
          String.format(
              "There is no %1$d units of product with id %2$d",
              newAmount, orderItemDto.getProductId()));
    }

    OrderDetails cart = getCartFromDb(sessionInfo.getCartId());
    orderItemDto = fillOrderItemInfo(orderItemDto, cart);
    cart.updateProductAmount(orderItemDto.getProductId(), newAmount);

    orderRepository.save(cart);
  }

  @Override
  public OrderDto checkout(SessionInfo sessionInfo) {
    sessionService.validateSessionInfo(sessionInfo);
    OrderDetails cart = getCartFromDb(sessionInfo.getCartId());
    if (cart.getTotal().doubleValue() <= 0) {
      throw new ConversionException("Empty cart cannot be convert to ordered order");
    }
    productService.validateAndUpdatePricesForOrder(cart);
    productService.updateAvailabilityForProductsIn(cart);

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

  @Override
  public void clearCart(SessionInfo sessionInfo) {
    sessionService.validateSessionInfo(sessionInfo);
    OrderDetails cart = getCartFromDb(sessionInfo.getCartId());
    cart.clearOrder();
    orderRepository.save(cart);
  }

  @Override
  public void addItem(OrderItemDto orderItemDto, SessionInfo sessionInfo) {
    sessionService.validateSessionInfo(sessionInfo);
    int amountAdded = orderItemDto.getQuantity();

    OrderDetails cart = getCartFromDb(sessionInfo.getCartId());
    orderItemDto = fillOrderItemInfo(orderItemDto, cart);
    Optional<OrderItem> itemFromCartOp = cart.getItemByProductId(orderItemDto.getProductId());
    int newAmount = amountAdded + itemFromCartOp.map(OrderItem::getQuantity).orElse(0);

    if (!productService.isAvailableProductWithAmount(orderItemDto.getProductId(), newAmount)) {
      throw new ProductNotAvailableException(
          String.format(
              "There is no %1$d units of product with id %2$d",
              orderItemDto.getQuantity(), orderItemDto.getProductId()));
    }

    cart.addProduct(productService.getProductById(orderItemDto.getProductId()), amountAdded);

    orderRepository.save(cart);
  }

  private User getClientFromDb(long userId) {
    Optional<User> userFromDb = userRepository.findById(userId);
    if (userFromDb.isEmpty()) {
      throw new UserNotFoundException(String.format("user with id %d not found", userId));
    }
    return userFromDb.get();
  }

  private OrderDetails getCartFromDb(long cartId) {
    Optional<OrderDetails> cartFromDb = orderRepository.findById(cartId);
    if (cartFromDb.isEmpty() || !OrderStatus.CART.equals(cartFromDb.get().getStatus())) {
      throw new CartNotFoundException(String.format("Cart with id %d not found", cartId));
    }
    return cartFromDb.get();
  }

  private OrderItemDto fillOrderItemInfo(OrderItemDto orderItemDto, OrderDetails cart) {
    OrderItemDto returnedOrderItem = new OrderItemDto(orderItemDto);
    if (orderItemDto.getProductId() == null) {

      if (orderItemDto.getOrdinal() >= cart.getItems().size()) {
        throw new ConversionException(
            String.format(
                "Wrong ordinal of the item. Cart doesn't have order item with ordinal %d",
                orderItemDto.getOrdinal()));
      }

      OrderItem itemFromCart = cart.getItems().get(orderItemDto.getOrdinal());
      long productIdFromCart = itemFromCart.getProductId();
      returnedOrderItem.setProductId(productIdFromCart);
    }
    return returnedOrderItem;
  }
}
