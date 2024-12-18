package com.griddynamics.shopapi.service.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.when;

import com.griddynamics.shopapi.dto.ProductDto;
import com.griddynamics.shopapi.exception.ProductNotAvailableException;
import com.griddynamics.shopapi.exception.ProductNotFoundException;
import com.griddynamics.shopapi.exception.WrongOrderException;
import com.griddynamics.shopapi.model.*;
import com.griddynamics.shopapi.repository.CartRepository;
import com.griddynamics.shopapi.repository.ProductRepository;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ProductServiceImplTest {

  @Mock ProductRepository productRepository;
  @Mock CartRepository cartRepository;
  @InjectMocks ProductServiceImpl productService;
  Product product1, product2, product3;
  List<Product> products;
  int available1, available3;
  int quantity1, quantity3;
  List<CartItem> items;
  CartItem item1, item3;

  @BeforeEach
  void setUp() {
    product1 = new Product();
    product1.setId(3252L);
    product1.setPrice(BigDecimal.valueOf(23.12));
    available1 = 10;
    product1.setAvailable(available1);
    product1.setTitle("product1");

    product2 = new Product();
    product2.setId(89L);
    product2.setPrice(BigDecimal.valueOf(1.2));
    product2.setAvailable(45);
    product2.setTitle("product2");

    product3 = new Product();
    product3.setId(7L);
    product3.setPrice(BigDecimal.valueOf(56));
    available3 = 4;
    product3.setAvailable(available3);
    product3.setTitle("product3");

    products = new ArrayList<>();
    products.add(product1);
    products.add(product2);
    products.add(product3);

    item1 = new CartItem();
    item1.setProduct(product1);
    quantity1 = 3;
    item1.setQuantity(quantity1);

    item3 = new CartItem();
    item3.setProduct(product3);
    quantity3 = 2;
    item3.setQuantity(quantity3);

    items = new ArrayList<>();
    items.add(item1);
    items.add(item3);
  }

  @Test
  void should_getAll_properRequest() {
    when(productRepository.findAll()).thenReturn(products);

    List<ProductDto> returned = productService.getAll();

    assertEquals(products.size(), returned.size());
    ProductDto dto = returned.get(0);
    assertEquals(product1.getId(), dto.getId());
    assertEquals(product1.getAvailable(), dto.getAvailable());
    assertEquals(product1.getPrice().doubleValue(), dto.getPrice());
    assertEquals(product1.getTitle(), dto.getTitle());
  }

  @Test
  void should_addItemsToAvailable_properRequest() {
    ArgumentCaptor<Set<Product>> captor = ArgumentCaptor.forClass(Set.class);
    when(productRepository.saveAll(captor.capture())).thenReturn(null);
    when(productRepository.findById(product1.getId())).thenReturn(Optional.of(product1));
    when(productRepository.findById(product3.getId())).thenReturn(Optional.of(product3));

    productService.addItemsToAvailable(items.stream().map(OrderItem::new).toList());

    Set<Product> captured = captor.getValue();

    assertFalse(captured.contains(product2));
    assertTrue(captured.contains(product1));
    assertTrue(captured.contains(product1));
    assertEquals(available1 + quantity1, product1.getAvailable());
    assertEquals(available3 + quantity3, product3.getAvailable());
  }

  @Test
  void should_validateAndUpdatePricesForOrder_properRequest() {
    Cart cart = new Cart();
    User user = new User();
    user.setId(4324L);
    cart.setUser(user);
    cart.setItems(items);
    cart.setTotal(
        product1
            .getPrice()
            .multiply(BigDecimal.valueOf(quantity1))
            .add(product3.getPrice().multiply(BigDecimal.valueOf(quantity3))));

    item1.setPrice(product1.getPrice());
    item3.setPrice(product3.getPrice());
    product1.setPrice(product1.getPrice().add(BigDecimal.ONE));
    product3.setPrice(product3.getPrice().add(BigDecimal.ONE));

    double newTotal =
        product1
            .getPrice()
            .multiply(BigDecimal.valueOf(quantity1))
            .add(product3.getPrice().multiply(BigDecimal.valueOf(quantity3)))
            .doubleValue();

    when(productRepository.findById(product1.getId())).thenReturn(Optional.of(product1));
    when(productRepository.findById(product3.getId())).thenReturn(Optional.of(product3));
    ArgumentCaptor<Cart> captor = ArgumentCaptor.forClass(Cart.class);
    when(cartRepository.save(captor.capture())).thenReturn(null);

    assertThrows(
        WrongOrderException.class, () -> productService.validateAndUpdatePricesForCart(cart));

    Cart captured = captor.getValue();

    assertEquals(newTotal, captured.getTotal().doubleValue());
    assertEquals(2, captured.getItems().size());
    assertTrue(
        captured.getItems().stream()
            .anyMatch(
                item ->
                    item.getProductId() == product1.getId()
                        && item.getPrice().equals(product1.getPrice())));
    assertTrue(
        captured.getItems().stream()
            .anyMatch(
                item ->
                    item.getProductId() == product3.getId()
                        && item.getPrice().equals(product3.getPrice())));
  }

  @Test
  void should_returnProperValue_when_isAvailableProductWithAmount() {
    when(productRepository.findById(product1.getId())).thenReturn(Optional.of(product1));

    assertTrue(
        productService.isAvailableProductWithAmount(product1.getId(), product1.getAvailable()));
    assertTrue(
        productService.isAvailableProductWithAmount(product1.getId(), product1.getAvailable() - 1));
    assertFalse(
        productService.isAvailableProductWithAmount(product1.getId(), product1.getAvailable() + 1));
  }

  @Test
  void should_getProductById_when_isInDb() {
    when(productRepository.findById(product1.getId())).thenReturn(Optional.of(product1));

    Product returned = productService.getProductById(product1.getId());

    assertEquals(product1, returned);
  }

  @Test
  void should_throwException_when_getProductById_when_notInDb() {
    when(productRepository.findById(product1.getId())).thenReturn(Optional.empty());

    assertThrows(
        ProductNotFoundException.class, () -> productService.getProductById(product1.getId()));
  }

  @Test
  void should_updateAvailabilityForProductsIn_when_available() {
    Cart cart = new Cart();
    cart.setItems(items);
    cart.setTotal(
        product1
            .getPrice()
            .multiply(BigDecimal.valueOf(quantity1))
            .add(product3.getPrice().multiply(BigDecimal.valueOf(quantity3))));

    when(productRepository.findById(product1.getId())).thenReturn(Optional.of(product1));
    when(productRepository.findById(product3.getId())).thenReturn(Optional.of(product3));
    ArgumentCaptor<Set<Product>> captor = ArgumentCaptor.forClass(Set.class);
    when(productRepository.saveAll(captor.capture())).thenReturn(null);

    productService.updateAvailabilityForProductsIn(cart);

    Set<Product> captured = captor.getValue();

    assertTrue(
        captured.stream()
            .anyMatch(
                product ->
                    product.getId().equals(product1.getId())
                        && product.getAvailable() == available1 - quantity1));
    assertTrue(
        captured.stream()
            .anyMatch(
                product ->
                    product.getId().equals(product3.getId())
                        && product.getAvailable() == available3 - quantity3));
    then(cartRepository).shouldHaveNoInteractions();
  }

  @Test
  void should_updateAvailabilityForProductsIn_when_notAvailable() {
    Cart cart = new Cart();
    item1.setQuantity(product1.getAvailable() + 3);
    cart.setItems(items);
    cart.setTotal(
        product1
            .getPrice()
            .multiply(BigDecimal.valueOf(available1 + 3))
            .add(product3.getPrice().multiply(BigDecimal.valueOf(quantity3))));

    double newTotal =
        product1
            .getPrice()
            .multiply(BigDecimal.valueOf(available1))
            .add(product3.getPrice().multiply(BigDecimal.valueOf(quantity3)))
            .doubleValue();

    when(productRepository.findById(product1.getId())).thenReturn(Optional.of(product1));
    when(productRepository.findById(product3.getId())).thenReturn(Optional.of(product3));
    ArgumentCaptor<Cart> captor = ArgumentCaptor.forClass(Cart.class);
    when(cartRepository.save(captor.capture())).thenReturn(null);

    assertThrows(
        ProductNotAvailableException.class,
        () -> productService.updateAvailabilityForProductsIn(cart));

    Cart captured = captor.getValue();

    then(productRepository).shouldHaveNoMoreInteractions();
    assertEquals(newTotal, captured.getTotal().doubleValue());
    assertEquals(available1, captured.getItems().get(0).getQuantity());
    assertEquals(quantity3, captured.getItems().get(1).getQuantity());
  }
}
