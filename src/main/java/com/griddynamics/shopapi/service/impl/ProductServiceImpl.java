package com.griddynamics.shopapi.service.impl;

import com.griddynamics.shopapi.dto.ProductDto;
import com.griddynamics.shopapi.exception.ProductNotAvailableException;
import com.griddynamics.shopapi.exception.ProductNotFoundException;
import com.griddynamics.shopapi.exception.WrongOrderException;
import com.griddynamics.shopapi.model.OrderDetails;
import com.griddynamics.shopapi.model.OrderItem;
import com.griddynamics.shopapi.model.Product;
import com.griddynamics.shopapi.repository.OrderRepository;
import com.griddynamics.shopapi.repository.ProductRepository;
import com.griddynamics.shopapi.service.ProductService;
import jakarta.transaction.Transactional;
import java.math.BigDecimal;
import java.util.*;
import org.springframework.stereotype.Service;

@Service
@Transactional
public class ProductServiceImpl implements ProductService {

  private final ProductRepository productRepository;
  private final OrderRepository orderRepository;

  public ProductServiceImpl(ProductRepository productRepository, OrderRepository orderRepository) {
    this.productRepository = productRepository;
    this.orderRepository = orderRepository;
  }

  @Override
  public List<ProductDto> getAll() {
    List<ProductDto> productsDto = new ArrayList<>();
    productRepository.findAll().forEach(product -> productsDto.add(new ProductDto(product)));
    return productsDto;
  }

  @Override
  public void addItemsToAvailable(List<OrderItem> items) {
    Set<Product> forSaving = new HashSet<>();

    items.forEach(
        item -> {
          Optional<Product> productOp = productRepository.findById(item.getProductId());
          if (productOp.isPresent()) {
            Product product = productOp.get();
            product.setAvailable(product.getAvailable() + item.getQuantity());
            forSaving.add(product);
          }
        });

    productRepository.saveAll(forSaving);
  }

  @Override
  public void validateAndUpdatePricesForOrder(OrderDetails order) {
    boolean wrongPrices = false;

    for (OrderItem item : order.getItems()) {
      Product product = getProductById(item.getProductId());
      if (!product.getPrice().equals(item.getPrice())) {
        order.setTotal(
            order
                .getTotal()
                .subtract(item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity()))));
        item.setPrice(product.getPrice());
        order.setTotal(
            order.getTotal().add(item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity()))));
        wrongPrices = true;
      }
    }

    if (wrongPrices) {
      orderRepository.save(order);
      throw new WrongOrderException(
          String.format(
              "Cart with id %d had wrong prices. Prices were updated, but ordered operation was aborted",
              order.getId()));
    }
  }

  @Override
  public boolean isAvailableProductWithAmount(long productId, int amount) {
    Product product = getProductById(productId);
    return product.getAvailable() >= amount;
  }

  @Override
  public Product getProductById(long productId) {
    Optional<Product> productOp = productRepository.findById(productId);
    if (productOp.isEmpty()) {
      throw new ProductNotFoundException(
          String.format("Product with id %d which doesn't exist", productId));
    }

    return productOp.get();
  }

  @Override
  public void updateAvailabilityForProductsIn(OrderDetails order) {
    boolean isAvailable = true;
    List<Product> productsFromDb = new ArrayList<>();

    for (OrderItem item : order.getItems()) {
      Product product = getProductById(item.getProductId());
      if (product.getAvailable() < item.getQuantity()) {
        isAvailable = false;
        order.setTotal(
            order
                .getTotal()
                .subtract(product.getPrice().multiply(BigDecimal.valueOf(item.getQuantity()))));
        item.setQuantity(product.getAvailable());
        order.setTotal(
            order
                .getTotal()
                .add(product.getPrice().multiply(BigDecimal.valueOf(item.getQuantity()))));
      } else {
        productsFromDb.add(product);
      }
    }
    if (!isAvailable) {
      orderRepository.save(order);
      throw new ProductNotAvailableException(
          "Products in a cart are unavailable now. Cart was updated, but ordered operation was aborted");
    }

    Set<Product> forSaving = new HashSet<>();
    for (int i = 0; i < productsFromDb.size(); i++) {
      Product product = productsFromDb.get(i);
      OrderItem item = order.getItems().get(i);

      assert product.getId() == item.getProductId();

      product.setAvailable(product.getAvailable() - item.getQuantity());
      forSaving.add(product);
    }
    productRepository.saveAll(forSaving);
  }
}
