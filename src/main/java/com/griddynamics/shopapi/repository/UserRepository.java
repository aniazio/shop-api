package com.griddynamics.shopapi.repository;

import com.griddynamics.shopapi.model.UserDetails;
import org.springframework.data.repository.CrudRepository;

public interface UserRepository extends CrudRepository<UserDetails, Long> {}
