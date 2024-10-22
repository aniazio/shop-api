package com.griddynamics.shopapi.model;

import com.griddynamics.shopapi.security.Encoder;
import jakarta.persistence.*;
import java.util.Objects;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Getter
@Setter
@NoArgsConstructor
@ToString
public class UserDetails {
  @Id
  @SequenceGenerator(name = "seqUser", sequenceName = "USERS_SEQ", allocationSize = 1)
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "seqUser")
  private Long id;

  @Column(nullable = false)
  private String email;

  @Column(nullable = false)
  private String password;

  @OneToOne(mappedBy = "userDetails")
  @ToString.Exclude
  private Session session;

  public void setPassword(String password) {
    this.password = Encoder.encode(password);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    UserDetails that = (UserDetails) o;

    return Objects.equals(email, that.email);
  }

  @Override
  public int hashCode() {
    return email != null ? email.hashCode() : 0;
  }
}
