package com.griddynamics.shopapi.model;

import com.griddynamics.shopapi.security.Encoder;
import jakarta.persistence.*;
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
public class Client {
  @Id
  @SequenceGenerator(name = "seqClient", sequenceName = "CLIENTS_SEQ", allocationSize = 1)
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "seqClient")
  private Long id;

  @Column(nullable = false)
  private String email;

  @Column(nullable = false)
  private String password;


  @OneToMany(mappedBy = "client", fetch = FetchType.LAZY)
  private Set<OrderDetails> orders = new HashSet<>();

  public void setPassword(String password) {
    this.password = Encoder.encode(password);
  }

  public void addOrder(OrderDetails order) {
    orders.add(order);
  }

  public void removeOrder(OrderDetails order) {
    orders.remove(order);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    Client that = (Client) o;

    return Objects.equals(email, that.email);
  }

  @Override
  public int hashCode() {
    return email != null ? email.hashCode() : 0;
  }
}
