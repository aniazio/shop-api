package com.griddynamics.shopapi.bootstrap;

import com.griddynamics.shopapi.model.*;
import com.griddynamics.shopapi.repository.*;
import java.time.LocalDateTime;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class PopulatingDb implements CommandLineRunner {

  private final CartRepository cartRepository;
  private final OrderRepository orderRepository;
  private final ProductRepository productRepository;
  private final SessionRepository sessionRepository;
  private final UserRepository userRepository;

  public PopulatingDb(
      CartRepository cartRepository,
      OrderRepository orderRepository,
      ProductRepository productRepository,
      SessionRepository sessionRepository,
      UserRepository userRepository) {
    this.cartRepository = cartRepository;
    this.orderRepository = orderRepository;
    this.productRepository = productRepository;
    this.sessionRepository = sessionRepository;
    this.userRepository = userRepository;
  }

  @Override
  public void run(String... args) throws Exception {
    Product product1 = new Product();
    product1.setAvailable(10);
    product1.setTitle("product1");
    product1.setPrice(10.12);

    Product product2 = new Product();
    product2.setAvailable(5);
    product2.setTitle("product2");
    product2.setPrice(8.02);

    Product product3 = new Product();
    product3.setAvailable(203);
    product3.setTitle("product3");
    product3.setPrice(0.45);

    Product savedProduct1 = productRepository.save(product1);
    Product savedProduct2 = productRepository.save(product2);
    Product savedProduct3 = productRepository.save(product3);

    UserDetails user1 = new UserDetails();
    user1.setEmail("user1@gmail.com");
    user1.setPassword("strong-password");

    UserDetails user2 = new UserDetails();
    user2.setEmail("user2@onet.pl");
    user2.setPassword("12345");

    userRepository.save(user1);
    userRepository.save(user2);

    Session session1 = new Session();
    session1.setUserDetails(user1);
    session1.setSessionId("sadlasjdkla");
    session1.setExpirationTime(LocalDateTime.of(2024, 12, 12, 0, 0));

    Cart cart = new Cart();
    cart.setTotal(0);
    cart.setCreatedAt(LocalDateTime.now());
    cart.addProduct(savedProduct2, 2);
    cart.addProduct(savedProduct1, 6);

    session1.addCart(cart);
    cart.setSession(session1);

    sessionRepository.save(session1);
    cartRepository.save(cart);

    OrderDetails order = new OrderDetails();
    order.setCart(cart);
    order.setStatus(OrderStatus.ORDERED);
    order.setCreatedAt(LocalDateTime.now());

    orderRepository.save(order);
  }
}
