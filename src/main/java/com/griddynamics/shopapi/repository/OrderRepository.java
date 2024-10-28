package com.griddynamics.shopapi.repository;

import com.griddynamics.shopapi.model.OrderDetails;
import java.util.Optional;
import java.util.Set;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

public interface OrderRepository extends CrudRepository<OrderDetails, Long> {

  @Query(
      value = "SELECT o.user_id FROM Order_Details o WHERE o.status='CART' AND o.id=?1",
      nativeQuery = true)
  Optional<Long> findUserIdByIdAndStatusIsCart(long id);

  @Query(
      value = "SELECT * FROM Order_Details o WHERE o.status='CART' AND o.user_id=?1",
      nativeQuery = true)
  Optional<OrderDetails> findCartByUserId(long clientId);

  Set<OrderDetails> findByUserId(long clientId);

  Optional<OrderDetails> findByIdAndUserId(long id, long clientId);
}
