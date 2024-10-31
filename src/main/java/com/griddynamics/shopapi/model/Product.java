package com.griddynamics.shopapi.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.PositiveOrZero;
import java.math.BigDecimal;
import java.util.Objects;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.validator.constraints.Length;

@Entity
@Getter
@Setter
@NoArgsConstructor
@ToString
public class Product {
  @Id
  @SequenceGenerator(name = "seqProduct", sequenceName = "PRODUCTS_SEQ", allocationSize = 1)
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "seqProduct")
  private Long id;

  @Column(nullable = false)
  @Length(min = 3, max = 200)
  private String title;

  @Column(nullable = false)
  @PositiveOrZero
  private int available;

  @Column(nullable = false)
  private BigDecimal price;

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    Product product = (Product) o;

    return Objects.equals(title, product.title);
  }

  @Override
  public int hashCode() {
    return title != null ? title.hashCode() : 0;
  }
}
