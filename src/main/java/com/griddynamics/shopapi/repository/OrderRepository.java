package com.griddynamics.shopapi.repository;

import com.griddynamics.shopapi.model.OrderDetails;
import java.util.Optional;
import java.util.Set;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

public interface OrderRepository extends CrudRepository<OrderDetails, Long> {

  @Query(
      value = "SELECT o.client_id FROM Order_Details o WHERE o.status='CART' AND o.id=?1",
      nativeQuery = true)
  Optional<Long> findClientIdByIdAndStatusIsCart(long id);

  @Query(
      value = "SELECT * FROM Order_Details o WHERE o.status='CART' AND o.client_id=?1",
      nativeQuery = true)
  Optional<OrderDetails> findCartByClientId(long clientId);

  Set<OrderDetails> findByClientId(long clientId);

  Optional<OrderDetails> findByIdAndClientId(long id, long clientId);
}
