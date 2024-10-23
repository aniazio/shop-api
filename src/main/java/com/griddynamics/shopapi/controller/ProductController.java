package com.griddynamics.shopapi.controller;

import com.griddynamics.shopapi.dto.ProductListDto;
import com.griddynamics.shopapi.service.ProductService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/products")
public class ProductController {

  private final ProductService productService;

  public ProductController(ProductService productService) {
    this.productService = productService;
  }


  @GetMapping("")
  public ProductListDto getAll() {
    return productService.getAll();
  }
}
