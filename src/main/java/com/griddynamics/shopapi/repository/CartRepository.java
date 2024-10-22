package com.griddynamics.shopapi.repository;

import com.griddynamics.shopapi.model.Cart;
import org.springframework.data.repository.CrudRepository;

public interface CartRepository extends CrudRepository<Cart, Long> {}
