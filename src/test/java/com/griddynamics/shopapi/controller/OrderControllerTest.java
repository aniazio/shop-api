package com.griddynamics.shopapi.controller;

import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.griddynamics.shopapi.dto.OrderDto;
import com.griddynamics.shopapi.model.OrderDetails;
import com.griddynamics.shopapi.model.OrderStatus;
import com.griddynamics.shopapi.model.User;
import com.griddynamics.shopapi.service.OrderService;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
class OrderControllerTest {
  @Mock OrderService orderService;
  @InjectMocks OrderController controller;
  MockMvc mockMvc;
  long userId = 4362;
  long cartId = 192;
  Map<String, Object> sessionAttrs;
  OrderDto orderDto1, orderDto2;

  @BeforeEach
  void setUp() {
    mockMvc =
        MockMvcBuilders.standaloneSetup(controller)
            .setControllerAdvice(new GlobalControllerAdvice())
            .build();

    sessionAttrs = new HashMap<>();
    sessionAttrs.put("userId", userId);
    sessionAttrs.put("cartId", cartId);

    OrderDetails orderDetails = new OrderDetails();
    orderDetails.setStatus(OrderStatus.ORDERED);
    orderDetails.setId(123L);
    User user = new User();
    user.setId(userId);
    orderDetails.setUser(user);

    orderDto1 = new OrderDto(orderDetails);

    orderDetails.setId(345L);
    orderDetails.setStatus(OrderStatus.CANCELED);
    orderDto2 = new OrderDto(orderDetails);
  }

  @Test
  void should_return200_when_getAllOrders() throws Exception {
    List<OrderDto> orders = new ArrayList<>();
    orders.add(orderDto1);
    orders.add(orderDto2);
    when(orderService.getAllOrderForUser(userId)).thenReturn(orders);

    mockMvc
        .perform(get("/orders").sessionAttrs(sessionAttrs))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content[0].id", Matchers.equalTo((int) orderDto1.getId())))
        .andExpect(
            jsonPath("$.content[0].status", Matchers.equalTo(OrderStatus.ORDERED.toString())))
        .andExpect(jsonPath("$.content[1].id", Matchers.equalTo((int) orderDto2.getId())))
        .andExpect(
            jsonPath("$.content[1].status", Matchers.equalTo(OrderStatus.CANCELED.toString())));
  }

  @Test
  void should_return_200_when_getOrder() throws Exception {
    when(orderService.getOrderForUser(userId, orderDto1.getId())).thenReturn(orderDto1);

    mockMvc
        .perform(get("/orders/" + orderDto1.getId()).sessionAttrs(sessionAttrs))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id", Matchers.equalTo((int) orderDto1.getId())))
        .andExpect(jsonPath("$.status", Matchers.equalTo(OrderStatus.ORDERED.toString())))
        .andExpect(jsonPath("$.userId").doesNotExist());
  }

  @Test
  void should_return200_when_deleteOrder() throws Exception {
    mockMvc
        .perform(delete("/orders/" + orderDto1.getId()).sessionAttrs(sessionAttrs))
        .andExpect(status().isOk());

    then(orderService).should().cancelOrder(userId, orderDto1.getId());
  }

  @Test
  void should_return401_when_unauthorized() throws Exception {
    mockMvc
        .perform(get("/orders"))
        .andExpect(status().isUnauthorized())
        .andExpect(jsonPath("$.title", Matchers.equalTo("Unauthorized")))
        .andExpect(jsonPath("$.detail", Matchers.containsString("Please, log in")));
  }
}
