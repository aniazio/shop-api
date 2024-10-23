package com.griddynamics.shopapi.dto;

import com.griddynamics.shopapi.model.Product;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@NoArgsConstructor
@ToString
public class ProductDto {
  private long id;
  private String title;
  private int available;
  private double price;

  public ProductDto(Product product) {
    id = product.getId();
    title = product.getTitle();
    available = product.getAvailable();
    price = product.getPrice();
  }
}
