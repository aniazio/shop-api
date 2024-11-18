package com.griddynamics.shopapi.model;

import com.griddynamics.shopapi.util.Encoder;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import java.util.HashSet;
import java.util.Locale;
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
@Table(name = "UserDetails")
public class User {
  @Id
  @SequenceGenerator(name = "seqUser", sequenceName = "USERS_SEQ", allocationSize = 1)
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "seqUser")
  private Long id;

  @Column(nullable = false)
  @Email
  private String email;

  @Column(nullable = false, name = "password")
  private String encodedPassword;

  @OneToMany(mappedBy = "user", fetch = FetchType.LAZY)
  private Set<OrderDetails> orders = new HashSet<>();

  public void encodeAndSetPassword(String password) {
    this.encodedPassword = Encoder.encode(password);
  }

  public void addOrder(OrderDetails order) {
    orders.add(order);
  }

  public void removeOrder(OrderDetails order) {
    orders.remove(order);
  }

  public void setEmail(String email) {
    this.email = email.toLowerCase(Locale.ROOT);
  }

  public String getEmail() {
    return email.toLowerCase(Locale.ROOT);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    User that = (User) o;

    return Objects.equals(email, that.email);
  }

  @Override
  public int hashCode() {
    return email != null ? email.hashCode() : 0;
  }
}
