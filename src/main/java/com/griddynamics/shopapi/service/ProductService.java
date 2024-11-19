package com.griddynamics.shopapi.service;

import com.griddynamics.shopapi.dto.ProductDto;
import com.griddynamics.shopapi.model.Cart;
import com.griddynamics.shopapi.model.OrderItem;
import com.griddynamics.shopapi.model.Product;
import java.util.List;

public interface ProductService {
  List<ProductDto> getAll();

  void addItemsToAvailable(List<OrderItem> items);

  void validateAndUpdatePricesForCart(Cart cart);

  boolean isAvailableProductWithAmount(long productId, int amount);

  Product getProductById(long productId);

  void updateAvailabilityForProductsIn(Cart cart);
}
