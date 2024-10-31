package com.griddynamics.shopapi.service;

import com.griddynamics.shopapi.dto.ProductDto;
import com.griddynamics.shopapi.model.OrderDetails;
import com.griddynamics.shopapi.model.OrderItem;
import com.griddynamics.shopapi.model.Product;
import java.util.List;

public interface ProductService {
  List<ProductDto> getAll();

  void resetAvailabilityWhenOrderCancel(List<OrderItem> items);

  void validateAndUpdatePricesForOrder(OrderDetails order);

  boolean isAvailableProductWithAmount(long productId, int amount);

  Product getProductById(long productId);

  void updateAvailabilityOfProductsIn(OrderDetails order);
}
