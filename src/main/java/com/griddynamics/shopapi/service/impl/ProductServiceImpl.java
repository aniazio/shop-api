package com.griddynamics.shopapi.service.impl;

import com.griddynamics.shopapi.dto.ProductDto;
import com.griddynamics.shopapi.dto.ProductListDto;
import com.griddynamics.shopapi.exception.WrongOrderException;
import com.griddynamics.shopapi.model.OrderDetails;
import com.griddynamics.shopapi.model.OrderItem;
import com.griddynamics.shopapi.model.Product;
import com.griddynamics.shopapi.repository.OrderRepository;
import com.griddynamics.shopapi.repository.ProductRepository;
import com.griddynamics.shopapi.service.ProductService;
import jakarta.transaction.Transactional;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
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
  public ProductListDto getAll() {
    ProductListDto productsDto = new ProductListDto();
    productRepository.findAll().forEach(product -> productsDto.addProduct(new ProductDto(product)));
    return productsDto;
  }

  @Override
  public void resetAvailabilityForOrderClearing(List<OrderItem> items) {
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
  public void validateAndUpdatePrices(OrderDetails order) {
    boolean wrongPrices = false;

    for (OrderItem item : order.getItems()) {
      Optional<Product> productOp = productRepository.findById(item.getProductId());
      if (productOp.isEmpty()) {
        throw new WrongOrderException(
            "Cart has product with id "
                + item.getProductId()
                + " which not exists. Please, delete this product from the cart");
      }

      Product product = productOp.get();
      if (!product.getPrice().equals(item.getPrice())) {
        order.removeProduct(item.getProductId());
        order.addProduct(product, item.getQuantity());
        wrongPrices = true;
      }
    }

    if (wrongPrices) {
      orderRepository.save(order);
      throw new WrongOrderException(
          "Cart with id"
              + order.getId()
              + " has wrong prices. Prices was updated. Please, resubmit order");
    }
  }
}
