package com.griddynamics.shopapi.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.Objects;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.CreationTimestamp;

@Entity
@Getter
@Setter
@NoArgsConstructor
@ToString
public class OrderDetails {
  @Id
  @SequenceGenerator(name = "seqOrder", sequenceName = "ORDERS_SEQ", allocationSize = 1)
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "seqOrder")
  private Long id;

  @Column(nullable = false)
  @CreationTimestamp
  private LocalDateTime createdAt;

  @OneToOne(fetch = FetchType.LAZY, optional = false)
  private Cart cart;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private OrderStatus status;

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    OrderDetails that = (OrderDetails) o;

    if (!Objects.equals(createdAt, that.createdAt)) return false;
    return Objects.equals(cart, that.cart);
  }

  @Override
  public int hashCode() {
    int result = createdAt != null ? createdAt.hashCode() : 0;
    result = 31 * result + (cart != null ? cart.hashCode() : 0);
    return result;
  }
}
