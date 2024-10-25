package com.griddynamics.shopapi.repository;

import com.griddynamics.shopapi.model.OrderItem;
import com.griddynamics.shopapi.model.OrderItemPrimaryKey;
import org.springframework.data.repository.Repository;

public interface OrderItemRepository extends Repository<OrderItem, OrderItemPrimaryKey> {

  void delete(OrderItem orderItem);
}
