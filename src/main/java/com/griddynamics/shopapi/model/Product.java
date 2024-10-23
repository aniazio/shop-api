package com.griddynamics.shopapi.model;

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
public class Product {
  @Id
  @SequenceGenerator(name = "seqProduct", sequenceName = "PRODUCTS_SEQ", allocationSize = 1)
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "seqProduct")
  private Long id;

  @Column(nullable = false)
  private String title;

  @Column(nullable = false)
  private int available;

  @Column(nullable = false)
  private double price;

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
