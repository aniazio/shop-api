package com.griddynamics.shopapi.repository;

import com.griddynamics.shopapi.model.OrderDetails;
import java.util.Optional;
import java.util.Set;
import org.springframework.data.repository.CrudRepository;

public interface OrderRepository extends CrudRepository<OrderDetails, Long> {

  Set<OrderDetails> findByUserId(long userId);

  Optional<OrderDetails> findByIdAndUserId(long id, long userId);
}
