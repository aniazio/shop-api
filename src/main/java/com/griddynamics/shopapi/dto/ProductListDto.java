package com.griddynamics.shopapi.dto;

import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
public class ProductListDto {

  List<ProductDto> products = new ArrayList<>();

  public void addProduct(ProductDto product) {
    products.add(product);
  }

  public void removeProduct(ProductDto product) {
    products.remove(product);
  }
}