package com.griddynamics.shopapi.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.griddynamics.shopapi.dto.ProductDto;
import com.griddynamics.shopapi.service.ProductService;
import java.util.List;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@ExtendWith(MockitoExtension.class)
class ProductControllerTest {

  @Mock ProductService productService;
  @InjectMocks ProductController controller;
  MockMvc mockMvc;

  ProductDto productDto1, productDto2;

  @BeforeEach
  void setUp() {
    mockMvc = MockMvcBuilders.standaloneSetup(controller).build();

    productDto1 = new ProductDto();
    productDto1.setId(1324L);
    productDto1.setPrice(33.21);
    productDto1.setAvailable(10);
    productDto1.setTitle("product1");

    productDto2 = new ProductDto();
    productDto2.setId(133L);
    productDto2.setPrice(32.1);
    productDto2.setAvailable(21);
    productDto2.setTitle("product2");
  }

  @Test
  void should_return200_when_getAll() throws Exception {
    List<ProductDto> products = List.of(productDto1, productDto2);
    when(productService.getAll()).thenReturn(products);

    mockMvc
        .perform(get("/products"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content[0].id", Matchers.equalTo((int) productDto1.getId())))
        .andExpect(jsonPath("$.content[0].title", Matchers.equalTo(productDto1.getTitle())))
        .andExpect(jsonPath("$.content[0].price", Matchers.equalTo(productDto1.getPrice())))
        .andExpect(jsonPath("$.content[1].id", Matchers.equalTo((int) productDto2.getId())))
        .andExpect(
            jsonPath("$.content[1].available", Matchers.equalTo(productDto2.getAvailable())));
  }
}
