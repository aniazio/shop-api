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

  @Id private Long id;

  @OneToOne(optional = false, fetch = FetchType.LAZY)
  @MapsId
  private Client client;

  private String token = UUID.randomUUID().toString();
  private LocalDateTime expiration_date = LocalDateTime.now().plusMinutes(1);

  public boolean isExpired() {
    return LocalDateTime.now().isAfter(expiration_date);
  }
}
