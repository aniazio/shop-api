package com.griddynamics.shopapi.repository;

import com.griddynamics.shopapi.model.ResetToken;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

public interface ResetTokenRepository extends CrudRepository<ResetToken, Long> {
  @Modifying
  @Query(value = "DELETE FROM reset_token t WHERE t.expiration_time < NOW()", nativeQuery = true)
  void deleteExpired();
}
