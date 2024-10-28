package com.griddynamics.shopapi.service;

import com.griddynamics.shopapi.dto.ProductListDto;
import com.griddynamics.shopapi.model.OrderDetails;
import com.griddynamics.shopapi.model.OrderItem;
import java.util.List;

public interface ProductService {
  ProductListDto getAll();

  void resetAvailabilityForOrderClearing(List<OrderItem> items);

  void validateAndUpdatePrices(OrderDetails order);
}
