package com.griddynamics.shopapi.service.impl;

import com.griddynamics.shopapi.dto.*;
import com.griddynamics.shopapi.exception.*;
import com.griddynamics.shopapi.model.*;
import com.griddynamics.shopapi.repository.CartRepository;
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

  private final CartRepository cartRepository;
  private final OrderRepository orderRepository;
  private final UserRepository userRepository;
  private final ProductService productService;

  @Override
  public CartDto getCartFor(long userId) {
    Cart cart = getCartFromDb(userId);
    return new CartDto(cart);
  }

  @Override
  public void deleteItemFromCart(long productId, long userId) {
    Cart cart = getCartFromDb(userId);
    cart.removeProduct(productId);
    cartRepository.save(cart);
  }

  @Override
  public void updateItemAmount(CartItemDto cartItemDto, long userId) {
    int newAmount = cartItemDto.getQuantity();
    if (!productService.isAvailableProductWithAmount(cartItemDto.getProductId(), newAmount)) {
      throw new ProductNotAvailableException(
          String.format(
              "There is no %1$d units of product with id %2$d",
              newAmount, cartItemDto.getProductId()));
    }

    Cart cart = getCartFromDb(userId);
    cartItemDto = fillCartItemInfo(cartItemDto, cart);
    cart.updateProductAmount(cartItemDto.getProductId(), newAmount);

    cartRepository.save(cart);
  }

  @Override
  public OrderDto checkout(long userId) {
    Cart cart = getCartFromDb(userId);
    if (cart.getTotal().doubleValue() <= 0) {
      throw new ConversionException("Empty cart cannot be convert to ordered order");
    }
    productService.validateAndUpdatePricesForCart(cart);
    productService.updateAvailabilityForProductsIn(cart);

    OrderDetails order = new OrderDetails(cart);
    order.copyItemList(cart.getItems());
    order.setStatus(OrderStatus.ORDERED);
    OrderDetails savedOrder = orderRepository.save(order);
    cartRepository.delete(cart);
    return new OrderDto(savedOrder);
  }

  @Override
  public void createNewCart(long userId) {
    User user = getClientFromDb(userId);
    Optional<Cart> existingCart = cartRepository.findByUserId(user.getId());
    if (existingCart.isPresent()) {
      throw new ForbiddenResourcesException(
          String.format("Cart for user with id %d already exists", user.getId()));
    }

    Cart newCart = new Cart();
    newCart.setUser(user);
    cartRepository.save(newCart);
  }

  @Override
  public void clearCart(long userId) {
    Cart cart = getCartFromDb(userId);
    cart.clearOrder();
    cartRepository.save(cart);
  }

  @Override
  public void addItem(CartItemDto cartItemDto, long userId) {
    int amountAdded = cartItemDto.getQuantity();

    Cart cart = getCartFromDb(userId);
    cartItemDto = fillCartItemInfo(cartItemDto, cart);
    Optional<CartItem> itemFromCartOp = cart.getItemByProductId(cartItemDto.getProductId());
    int newAmount = amountAdded + itemFromCartOp.map(CartItem::getQuantity).orElse(0);

    if (!productService.isAvailableProductWithAmount(cartItemDto.getProductId(), newAmount)) {
      throw new ProductNotAvailableException(
          String.format(
              "There is no %1$d units of product with id %2$d",
              cartItemDto.getQuantity(), cartItemDto.getProductId()));
    }

    cart.addProduct(productService.getProductById(cartItemDto.getProductId()), amountAdded);

    cartRepository.save(cart);
  }

  private User getClientFromDb(long userId) {
    Optional<User> userFromDb = userRepository.findById(userId);
    if (userFromDb.isEmpty()) {
      throw new UserNotFoundException(String.format("user with id %d not found", userId));
    }
    return userFromDb.get();
  }

  private Cart getCartFromDb(long userId) {
    Optional<Cart> cartFromDb = cartRepository.findByUserId(userId);
    if (cartFromDb.isEmpty()) {
      throw new CartNotFoundException(String.format("Cart for user with id %d not found", userId));
    }
    return cartFromDb.get();
  }

  private CartItemDto fillCartItemInfo(CartItemDto cartItemDto, Cart cart) {
    CartItemDto returnedOrderItem = new CartItemDto(cartItemDto);
    if (cartItemDto.getProductId() == null) {

      if (cartItemDto.getOrdinal() == null
          || cartItemDto.getOrdinal() >= cart.getItems().size()
          || cartItemDto.getOrdinal() < 0) {
        throw new ConversionException(
            String.format(
                "Wrong ordinal of the item. Cart doesn't have order item with ordinal %d",
                cartItemDto.getOrdinal()));
      }

      CartItem itemFromCart = cart.getItems().get(cartItemDto.getOrdinal());
      long productIdFromCart = itemFromCart.getProductId();
      returnedOrderItem.setProductId(productIdFromCart);
    }
    return returnedOrderItem;
  }
}
