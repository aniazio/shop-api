package com.griddynamics.shopapi.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.griddynamics.shopapi.model.Product;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.validator.constraints.Length;

@Getter
@Setter
@NoArgsConstructor
@ToString
public class ProductDto {
  @JsonProperty(access = JsonProperty.Access.READ_ONLY)
  private long id;

  @Length(min = 3, max = 200)
  private String title;

  @PositiveOrZero private int available;

  @Positive
  @Digits(integer = 6, fraction = 2)
  private double price;

  public ProductDto(Product product) {
    id = product.getId();
    title = product.getTitle();
    available = product.getAvailable();
    price = product.getPrice().doubleValue();
  }
}
