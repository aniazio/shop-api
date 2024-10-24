package com.griddynamics.shopapi.service;

import com.griddynamics.shopapi.dto.CartDto;
import com.griddynamics.shopapi.dto.OrderDto;
import com.griddynamics.shopapi.dto.OrderItemDto;
import com.griddynamics.shopapi.dto.SessionInfo;
import com.griddynamics.shopapi.exception.*;
import com.griddynamics.shopapi.model.Client;
import com.griddynamics.shopapi.model.OrderDetails;
import com.griddynamics.shopapi.model.OrderStatus;
import com.griddynamics.shopapi.model.Product;
import com.griddynamics.shopapi.repository.ClientRepository;
import com.griddynamics.shopapi.repository.OrderRepository;
import com.griddynamics.shopapi.repository.ProductRepository;
import java.util.Optional;
import org.springframework.stereotype.Service;

@Service
public class CartServiceImpl implements CartService {

  private final OrderRepository orderRepository;
  private final ProductRepository productRepository;
  private final ClientRepository clientRepository;

  public CartServiceImpl(
      OrderRepository orderRepository,
      ProductRepository productRepository,
      ClientRepository clientRepository) {
    this.orderRepository = orderRepository;
    this.productRepository = productRepository;
    this.clientRepository = clientRepository;
  }

  @Override
  public CartDto getCartFor(SessionInfo sessionInfo) {
    validateSessionInfo(sessionInfo);
    OrderDetails cart = getCartFromDb(sessionInfo.getCartId());
    return new CartDto(cart);
  }

  @Override
  public CartDto getCartFor(long clientId) {
    Optional<OrderDetails> cartFromDb = orderRepository.findCartByClientId(clientId);
    if (cartFromDb.isEmpty() || cartFromDb.get().getStatus().equals(OrderStatus.CART)) {
      throw new CartNotFoundException("Cart not found for client with id " + clientId);
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
    cart.setStatus(OrderStatus.ORDERED);
    orderRepository.save(cart);
    return new OrderDto(cart);
  }

  @Override
  public long getIdOfNewCart(SessionInfo sessionInfo) {
    validateSessionInfo(sessionInfo);

    Client client = getClientFromDb(sessionInfo.getUserId());
    Optional<OrderDetails> existingCart = orderRepository.findCartByClientId(client.getId());
    if (existingCart.isPresent()) {
      throw new ForbiddenResourcesException("Cart for these user already exists");
    }

    OrderDetails newCart = new OrderDetails();
    newCart.setStatus(OrderStatus.CART);
    newCart.setClient(client);
    OrderDetails savedCart = orderRepository.save(newCart);

    return savedCart.getId();
  }

  private Client getClientFromDb(long clientId) {
    Optional<Client> clientFromDb = clientRepository.findById(clientId);
    if (clientFromDb.isEmpty()) {
      throw new ClientNotFoundException("client with id " + clientId + " not found");
    }
    return clientFromDb.get();
  }

  @Override
  public void clearCart(SessionInfo sessionInfo) {
    validateSessionInfo(sessionInfo);
    OrderDetails cart = getCartFromDb(sessionInfo.getCartId());
    cart.clearOrder();
    orderRepository.save(cart);
  }

  private OrderDetails getCartFromDb(long cartId) {
    Optional<OrderDetails> cartFromDb = orderRepository.findById(cartId);
    if (cartFromDb.isEmpty() || cartFromDb.get().getStatus().equals(OrderStatus.CART)) {
      throw new CartNotFoundException("Cart with id " + cartId + " not found");
    }
    return cartFromDb.get();
  }

  private void validateSessionInfo(SessionInfo sessionInfo) {
    Optional<Long> clientIdFromCart =
        orderRepository.findClientIdByIdAndStatusIsCart(sessionInfo.getCartId());
    if (clientIdFromCart.isEmpty()) {
      throw new CartNotFoundException("cart with id " + sessionInfo.getCartId() + " not found");
    }
    if (clientIdFromCart.get().equals(sessionInfo.getUserId())) {
      throw new ForbiddenResourcesException(
          "userId "
              + sessionInfo.getUserId()
              + " doesn't match info for cart of the id "
              + sessionInfo.getCartId());
    }
  }
}
