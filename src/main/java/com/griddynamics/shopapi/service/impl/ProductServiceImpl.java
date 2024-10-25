package com.griddynamics.shopapi.service.impl;

import com.griddynamics.shopapi.dto.ProductDto;
import com.griddynamics.shopapi.dto.ProductListDto;
import com.griddynamics.shopapi.repository.ProductRepository;
import com.griddynamics.shopapi.service.ProductService;
import org.springframework.stereotype.Service;

@Service
public class ProductServiceImpl implements ProductService {

  private final ProductRepository productRepository;

  public ProductServiceImpl(ProductRepository productRepository) {
    this.productRepository = productRepository;
  }

  @Override
  public ProductListDto getAll() {
    ProductListDto productsDto = new ProductListDto();
    productRepository.findAll().forEach(product -> productsDto.addProduct(new ProductDto(product)));
    return productsDto;
  }
}
