package com.griddynamics.shopapi.service.impl;

import com.griddynamics.shopapi.dto.ProductDto;
import com.griddynamics.shopapi.exception.ProductNotAvailableException;
import com.griddynamics.shopapi.exception.WrongOrderException;
import com.griddynamics.shopapi.model.OrderDetails;
import com.griddynamics.shopapi.model.OrderItem;
import com.griddynamics.shopapi.model.Product;
import com.griddynamics.shopapi.repository.OrderRepository;
import com.griddynamics.shopapi.repository.ProductRepository;
import com.griddynamics.shopapi.service.ProductService;
import jakarta.transaction.Transactional;
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
  public void resetAvailabilityWhenOrderCancel(List<OrderItem> items) {
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
        order.removeProduct(item.getProductId());
        order.addProduct(product, item.getQuantity());
        wrongPrices = true;
      }
    }

    if (wrongPrices) {
      orderRepository.save(order);
      throw new WrongOrderException(
          String.format(
              "Cart with id %d had wrong prices. Prices were updated. Please, resubmit order",
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
      throw new WrongOrderException(
          String.format(
              "Cart has product with id %d which doesn't exist. Please, delete this product from the cart",
              productId));
    }

    return productOp.get();
  }

  @Override
  public void updateAvailabilityOfProductsIn(OrderDetails order) {
    boolean isAvailable = true;
    List<Product> productsFromDb = new ArrayList<>();

    for (OrderItem item : order.getItems()) {
      Product product = getProductById(item.getProductId());
      if (product.getAvailable() < item.getQuantity()) {
        isAvailable = false;
        item.setQuantity(product.getAvailable());
      } else {
        productsFromDb.add(product);
      }
    }
    if (!isAvailable) {
      orderRepository.save(order);
      throw new ProductNotAvailableException(
          "Products in a cart are unavailable now. Cart was updated. Try to checkout again");
    }

    for (int i = 0; i < productsFromDb.size(); i++) {
      Product product = productsFromDb.get(i);
      OrderItem item = order.getItems().get(i);

      assert product.getId() == item.getProductId();

      product.setAvailable(product.getAvailable() - item.getQuantity());
    }
  }
}
