package com.griddynamics.shopapi.repository;

import com.griddynamics.shopapi.model.OrderDetails;
import org.springframework.data.repository.CrudRepository;

public interface OrderRepository extends CrudRepository<OrderDetails, Long> {}
