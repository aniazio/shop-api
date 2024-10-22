package com.griddynamics.shopapi.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Getter
@Setter
@NoArgsConstructor
@ToString
public class Session {

  @Id
  @SequenceGenerator(name = "seqSession", sequenceName = "SESSIONS_SEQ", allocationSize = 1)
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "seqSession")
  private Long id;

  @Column(nullable = false)
  private String sessionId;

  @Column(nullable = false)
  private LocalDateTime expirationTime;

  @OneToOne(fetch = FetchType.LAZY)
  private UserDetails userDetails;

  @OneToMany(mappedBy = "session")
  @ToString.Exclude
  private Set<Cart> carts = new HashSet<>();

  public void addCart(Cart cart) {
    carts.add(cart);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    Session session = (Session) o;

    return Objects.equals(sessionId, session.sessionId);
  }

  @Override
  public int hashCode() {
    return sessionId != null ? sessionId.hashCode() : 0;
  }
}
