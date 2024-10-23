package com.griddynamics.shopapi.bootstrap;

import com.griddynamics.shopapi.model.*;
import com.griddynamics.shopapi.repository.*;
import java.time.LocalDateTime;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class PopulatingDb implements CommandLineRunner {

  private final OrderRepository orderRepository;
  private final ProductRepository productRepository;
  private final ClientRepository clientRepository;

  public PopulatingDb(
      OrderRepository orderRepository,
      ProductRepository productRepository,
      ClientRepository clientRepository) {
    this.orderRepository = orderRepository;
    this.productRepository = productRepository;
    this.clientRepository = clientRepository;
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

    Client client1 = new Client();
    client1.setEmail("user1@gmail.com");
    client1.setPassword("strong-password");

    Client client2 = new Client();
    client2.setEmail("user2@onet.pl");
    client2.setPassword("12345");


    clientRepository.save(client1);
    clientRepository.save(client2);

    OrderDetails cart = new OrderDetails();
    cart.setTotal(0);
    cart.setCreatedAt(LocalDateTime.now());
    cart.addProduct(savedProduct2, 2);
    cart.addProduct(savedProduct1, 6);
    cart.setStatus(OrderStatus.CART);

    cart.setClient(client1);

    orderRepository.save(cart);
  }
}
