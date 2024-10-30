package com.griddynamics.shopapi.controller;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

import com.griddynamics.shopapi.dto.ProductDto;
import com.griddynamics.shopapi.service.ProductService;
import java.util.List;
import org.springframework.hateoas.CollectionModel;
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
  public CollectionModel<ProductDto> getAll() {
    List<ProductDto> returned = productService.getAll();

    CollectionModel<ProductDto> response = CollectionModel.of(returned);
    response.add(linkTo(methodOn(this.getClass()).getAll()).withSelfRel());
    return response;
  }
}
