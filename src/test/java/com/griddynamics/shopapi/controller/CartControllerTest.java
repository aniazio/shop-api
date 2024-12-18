package com.griddynamics.shopapi.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.griddynamics.shopapi.dto.*;
import com.griddynamics.shopapi.exception.CartNotFoundException;
import com.griddynamics.shopapi.exception.ForbiddenResourcesException;
import com.griddynamics.shopapi.exception.UserNotFoundException;
import com.griddynamics.shopapi.model.*;
import com.griddynamics.shopapi.service.CartService;
import com.griddynamics.shopapi.service.impl.SessionServiceImpl;
import jakarta.servlet.http.HttpSession;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@ExtendWith(MockitoExtension.class)
class CartControllerTest {

  @Mock CartService cartService;
  @Mock SessionServiceImpl sessionService;
  @InjectMocks CartController controller;
  @Captor ArgumentCaptor<HttpSession> sessionCaptor;
  MockMvc mockMvc;
  CartDto cart;
  CartItem item;
  long userId = 3242;
  Map<String, Object> sessionAttrs;
  static ObjectMapper mapper = new ObjectMapper();

  @BeforeEach
  void setUp() {

    mockMvc =
        MockMvcBuilders.standaloneSetup(controller)
            .setControllerAdvice(new GlobalControllerAdvice())
            .build();

    Cart cartEntity = new Cart();
    User user = new User();
    user.setId(userId);
    cartEntity.setUser(user);
    cartEntity.setTotal(BigDecimal.valueOf(10.10));
    cartEntity.setCreatedAt(LocalDateTime.now());
    item = new CartItem();
    item.setQuantity(10);
    item.setPrice(BigDecimal.valueOf(1.01));
    Product product = new Product();
    product.setId(101L);
    product.setPrice(BigDecimal.valueOf(1.01));
    item.setProduct(product);
    cartEntity.setItems(List.of(item));

    cart = new CartDto(cartEntity);

    sessionAttrs = new HashMap<>();
    sessionAttrs.put("userId", userId);
  }

  void verifySessionAttributes() {
    then(sessionService).should().getUserId(any());
    HttpSession captured = sessionCaptor.getValue();
    assertEquals(userId, (long) captured.getAttribute("userId"));
  }

  @Test
  void should_return200_when_getCart() throws Exception {
    when(sessionService.getUserId(sessionCaptor.capture())).thenReturn(userId);
    when(cartService.getCartFor(anyLong())).thenReturn(cart);

    mockMvc
        .perform(get("/cart").sessionAttrs(sessionAttrs))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.total", Matchers.equalTo(cart.getTotal())))
        .andExpect(jsonPath("$.items", Matchers.hasSize(1)))
        .andExpect(jsonPath("$.items[0].productId", Matchers.equalTo((int) item.getProductId())));

    verifySessionAttributes();
  }

  @Test
  void should_return200_when_getItems() throws Exception {
    when(sessionService.getUserId(sessionCaptor.capture())).thenReturn(userId);
    when(cartService.getCartFor(anyLong())).thenReturn(cart);

    mockMvc
        .perform(get("/cart/items").sessionAttrs(sessionAttrs))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content", Matchers.hasSize(1)))
        .andExpect(jsonPath("$.content[0].productId", Matchers.equalTo((int) item.getProductId())))
        .andExpect(jsonPath("$.content[0].price", Matchers.equalTo(item.getPrice().doubleValue())));

    verifySessionAttributes();
  }

  @Test
  void should_return200_when_deleteItemFromCart() throws Exception {
    when(sessionService.getUserId(sessionCaptor.capture())).thenReturn(userId);
    long productId = 33562L;

    mockMvc
        .perform(delete("/cart/items/" + (int) productId).sessionAttrs(sessionAttrs))
        .andExpect(status().isOk());

    then(cartService).should().deleteItemFromCart(productId, userId);
    then(sessionService).should().getUserId(any());
  }

  @Test
  void should_return201_when_addItemToCart_productId() throws Exception {
    when(sessionService.getUserId(sessionCaptor.capture())).thenReturn(userId);
    CartItemDto requestDto = new CartItemDto();
    requestDto.setProductId(1034L);
    requestDto.setQuantity(10);

    mockMvc
        .perform(
            post("/cart/items")
                .sessionAttrs(sessionAttrs)
                .content(mapper.writeValueAsString(requestDto))
                .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isCreated())
        .andExpect(header().exists("Location"));

    verifySessionAttributes();
    then(cartService).should().addItem(any(CartItemDto.class), eq(userId));
  }

  @Test
  void should_return201_when_addItemToCart_id() throws Exception {
    when(sessionService.getUserId(sessionCaptor.capture())).thenReturn(userId);
    CartItemDto requestDto = new CartItemDto();
    requestDto.setOrdinal(1);
    requestDto.setQuantity(4);

    mockMvc
        .perform(
            post("/cart/items")
                .sessionAttrs(sessionAttrs)
                .content(mapper.writeValueAsString(requestDto))
                .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isCreated())
        .andExpect(header().exists("Location"));

    verifySessionAttributes();
    then(cartService).should().addItem(any(CartItemDto.class), eq(userId));
  }

  @Test
  void should_return400_when_addItemToCart_withoutId() throws Exception {
    CartItemDto requestDto = new CartItemDto();
    requestDto.setQuantity(324);

    mockMvc
        .perform(
            post("/cart/items")
                .sessionAttrs(sessionAttrs)
                .content(mapper.writeValueAsString(requestDto))
                .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.title", Matchers.equalTo("Validation error")))
        .andExpect(jsonPath("$.detail", Matchers.containsString("Id or productId required")));

    then(cartService).shouldHaveNoInteractions();
  }

  @Test
  void should_return200_when_updateItemAmount_productId() throws Exception {
    when(sessionService.getUserId(sessionCaptor.capture())).thenReturn(userId);
    CartItemDto requestDto = new CartItemDto();
    requestDto.setProductId(114L);
    requestDto.setQuantity(10);

    mockMvc
        .perform(
            patch("/cart/items")
                .sessionAttrs(sessionAttrs)
                .content(mapper.writeValueAsString(requestDto))
                .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(header().exists("Location"));

    verifySessionAttributes();
    then(cartService).should().updateItemAmount(any(CartItemDto.class), eq(userId));
  }

  @Test
  void should_return200_when_updateItemAmount_id() throws Exception {
    when(sessionService.getUserId(sessionCaptor.capture())).thenReturn(userId);
    CartItemDto requestDto = new CartItemDto();
    requestDto.setOrdinal(1);
    requestDto.setQuantity(10);

    mockMvc
        .perform(
            patch("/cart/items")
                .sessionAttrs(sessionAttrs)
                .content(mapper.writeValueAsString(requestDto))
                .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(header().exists("Location"));

    verifySessionAttributes();
    then(cartService).should().updateItemAmount(any(CartItemDto.class), eq(userId));
  }

  @Test
  void should_return400_when_updateItemAmount_withoutQuantity() throws Exception {
    OrderItemDto requestDto = new OrderItemDto();
    requestDto.setOrdinal(3);

    mockMvc
        .perform(
            post("/cart/items")
                .sessionAttrs(sessionAttrs)
                .content(mapper.writeValueAsString(requestDto))
                .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.title", Matchers.equalTo("Validation error")))
        .andExpect(jsonPath("$.detail", Matchers.containsString("Quantity must be positive")));

    then(cartService).shouldHaveNoInteractions();
  }

  @Test
  void should_return200_when_checkout() throws Exception {
    when(sessionService.getUserId(sessionCaptor.capture())).thenReturn(userId);
    OrderDetails returned = new OrderDetails();
    returned.setId(191L);
    returned.setStatus(OrderStatus.ORDERED);
    User user = new User();
    user.setId(userId);
    returned.setUser(user);

    when(cartService.checkout(anyLong())).thenReturn(new OrderDto(returned));

    mockMvc
        .perform(put("/cart/checkout").sessionAttrs(sessionAttrs))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.userId").doesNotExist())
        .andExpect(jsonPath("$.status", Matchers.equalTo(OrderStatus.ORDERED.toString())))
        .andExpect(jsonPath("$.id", Matchers.equalTo(returned.getId().intValue())));

    then(sessionService).should().getUserId(any());
    HttpSession captured = sessionCaptor.getValue();
    assertEquals(userId, (long) captured.getAttribute("userId"));
  }

  @Test
  void should_return200_when_clearCart() throws Exception {
    when(sessionService.getUserId(sessionCaptor.capture())).thenReturn(userId);

    mockMvc
        .perform(delete("/cart").sessionAttrs(sessionAttrs))
        .andExpect(status().isOk())
        .andExpect(header().exists("location"));

    verifySessionAttributes();
    then(cartService).should().clearCart(userId);
  }

  @Test
  void should_return404_when_resourceNotFound() throws Exception {
    when(sessionService.getUserId(sessionCaptor.capture())).thenReturn(userId);
    when(cartService.getCartFor(anyLong())).thenThrow(UserNotFoundException.class);

    mockMvc
        .perform(get("/cart").sessionAttrs(sessionAttrs))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.title", Matchers.equalTo("Resource not found")));

    verifySessionAttributes();
  }

  @Test
  void should_return404_when_cartNotFound() throws Exception {
    when(sessionService.getUserId(sessionCaptor.capture())).thenReturn(userId);
    when(cartService.getCartFor(anyLong())).thenThrow(CartNotFoundException.class);

    mockMvc
        .perform(get("/cart").sessionAttrs(sessionAttrs))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.title", Matchers.equalTo("Resource not found")))
        .andExpect(jsonPath("$.detail", Matchers.containsString("try to log in again")));

    verifySessionAttributes();
  }

  @Test
  void should_return403_when_forbidden() throws Exception {
    when(sessionService.getUserId(sessionCaptor.capture())).thenReturn(userId);
    when(cartService.getCartFor(anyLong())).thenThrow(ForbiddenResourcesException.class);

    mockMvc
        .perform(get("/cart").sessionAttrs(sessionAttrs))
        .andExpect(status().isForbidden())
        .andExpect(jsonPath("$.title", Matchers.equalTo("Forbidden resource")))
        .andExpect(jsonPath("$.detail", Matchers.containsString("forbidden resource")));

    verifySessionAttributes();
  }
}
