package com.griddynamics.shopapi.controller;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

import com.griddynamics.shopapi.dto.ProductDto;
import com.griddynamics.shopapi.service.ProductService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.hateoas.CollectionModel;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/products")
@Slf4j
@RequiredArgsConstructor
public class ProductController {

  private final ProductService productService;

  @GetMapping
  public CollectionModel<ProductDto> getAll() {
    List<ProductDto> productDtos = productService.getAll();

    CollectionModel<ProductDto> response = CollectionModel.of(productDtos);
    response.add(linkTo(methodOn(this.getClass()).getAll()).withSelfRel());
    return response;
  }
}
