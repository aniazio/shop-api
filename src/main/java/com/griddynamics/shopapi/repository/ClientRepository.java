package com.griddynamics.shopapi.repository;

import com.griddynamics.shopapi.model.Client;
import java.util.Optional;
import org.springframework.data.repository.CrudRepository;

public interface ClientRepository extends CrudRepository<Client, Long> {

  Optional<Client> findByEmail(String email);
}
