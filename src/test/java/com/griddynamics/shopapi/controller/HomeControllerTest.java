package com.griddynamics.shopapi.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.HashMap;
import java.util.Map;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

class HomeControllerTest {

  HomeController controller;
  MockMvc mockMvc;
  long userId = 3242;
  long cartId = 657;
  Map<String, Object> sessionAttrs;

  @BeforeEach
  void setUp() {
    controller = new HomeController();
    mockMvc = MockMvcBuilders.standaloneSetup(controller).build();

    sessionAttrs = new HashMap<>();
    sessionAttrs.put("userId", userId);
    sessionAttrs.put("cartId", cartId);
  }

  @Test
  void should_getHome_when_logged() throws Exception {
    mockMvc
        .perform(get("/").sessionAttrs(sessionAttrs))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].rel", Matchers.equalTo("products")))
        .andExpect(jsonPath("$[1].rel", Matchers.equalTo("loginForm")))
        .andExpect(jsonPath("$[2].rel", Matchers.equalTo("cart")))
        .andExpect(jsonPath("$[3].rel", Matchers.equalTo("orders")));
  }

  @Test
  void should_getHome_when_notLogged() throws Exception {
    mockMvc
        .perform(get("/"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].rel", Matchers.equalTo("products")))
        .andExpect(jsonPath("$[1].rel", Matchers.equalTo("loginForm")));
  }
}
