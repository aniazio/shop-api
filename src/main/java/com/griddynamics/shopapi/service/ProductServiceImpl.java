package com.griddynamics.shopapi.service;

import com.griddynamics.shopapi.dto.ProductDto;
import com.griddynamics.shopapi.dto.ProductListDto;
import com.griddynamics.shopapi.repository.ProductRepository;
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
