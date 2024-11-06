package com.griddynamics.shopapi.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Entity
@NoArgsConstructor
public class ResetToken {

  public static final int EXPIRATION_TIME_FOR_RESET_TOKENS = 1;

  @Id private Long id;

  @OneToOne(optional = false, fetch = FetchType.LAZY)
  @MapsId
  private User user;

  private String token = UUID.randomUUID().toString();
  private LocalDateTime expirationTime =
      LocalDateTime.now().plusMinutes(EXPIRATION_TIME_FOR_RESET_TOKENS);

  public boolean isExpired() {
    return LocalDateTime.now().isAfter(expirationTime);
  }
}
