package com.griddynamics.shopapi.repository;

import com.griddynamics.shopapi.model.Cart;
import java.util.Optional;
import org.springframework.data.repository.CrudRepository;

public interface CartRepository extends CrudRepository<Cart, Long> {

  Optional<Cart> findByUserId(long userId);
}
