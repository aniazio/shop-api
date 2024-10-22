package com.griddynamics.shopapi.repository;

import com.griddynamics.shopapi.model.Product;
import org.springframework.data.repository.CrudRepository;

public interface ProductRepository extends CrudRepository<Product, Long> {}
