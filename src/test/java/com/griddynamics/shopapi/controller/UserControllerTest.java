package com.griddynamics.shopapi.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.griddynamics.shopapi.dto.CartDto;
import com.griddynamics.shopapi.dto.UserDto;
import com.griddynamics.shopapi.exception.UserAlreadyExistsException;
import com.griddynamics.shopapi.exception.WrongCredentialsException;
import com.griddynamics.shopapi.model.OrderDetails;
import com.griddynamics.shopapi.model.OrderStatus;
import com.griddynamics.shopapi.model.User;
import com.griddynamics.shopapi.service.UserService;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {

  @Mock UserService userService;
  @InjectMocks UserController controller;
  MockMvc mockMvc;
  UserDto userDto;
  static ObjectMapper mapper = new ObjectMapper();

  @BeforeEach
  void setUp() {
    mockMvc =
        MockMvcBuilders.standaloneSetup(controller)
            .setControllerAdvice(new GlobalControllerAdvice())
            .build();
    userDto = new UserDto();
    userDto.setEmail("some@mail.com");
    userDto.setPassword("5679sdf#");
  }

  @Test
  void should_return200_when_loginUser() throws Exception {
    OrderDetails orderDetails = new OrderDetails();
    orderDetails.setStatus(OrderStatus.CART);
    orderDetails.setId(324L);
    User user = new User();
    user.setId(35L);
    orderDetails.setUser(user);

    CartDto cartDto = new CartDto(orderDetails);
    when(userService.loginAndReturnCart(any(UserDto.class))).thenReturn(cartDto);

    mockMvc
        .perform(
            put("/users/login")
                .content(mapper.writeValueAsString(userDto))
                .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.userId", Matchers.equalTo(user.getId().intValue())))
        .andExpect(jsonPath("$.cartId", Matchers.equalTo(orderDetails.getId().intValue())))
        .andExpect(jsonPath("$.sessionId").exists());
  }

  @Test
  void should_return201_when_registerUser() throws Exception {
    mockMvc
        .perform(
            post("/users/register")
                .content(mapper.writeValueAsString(userDto))
                .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isCreated());

    then(userService).should().register(any(UserDto.class));
  }

  @Test
  void should_return200_when_requestPasswordReset() throws Exception {
    userDto.setPassword(null);
    String response = "password reset performed";
    when(userService.startResettingPassword(any(UserDto.class))).thenReturn(response);

    mockMvc
        .perform(
            patch("/users/reset")
                .content(mapper.writeValueAsString(userDto))
                .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(content().string(response));
  }

  @Test
  void should_return200_when_resetPassword() throws Exception {
    String token = "sfssdf-asda-fsda";
    mockMvc
        .perform(
            put("/users/reset")
                .content(mapper.writeValueAsString(userDto))
                .contentType(MediaType.APPLICATION_JSON)
                .param("reset-token", token))
        .andExpect(status().isOk());

    then(userService).should().resetPassword(any(UserDto.class), eq(token));
  }

  @Test
  void should_return401_when_register_inValidUserData() throws Exception {
    userDto.setEmail("sdf");
    userDto.setPassword("234");

    mockMvc
        .perform(
            post("/users/register")
                .content(mapper.writeValueAsString(userDto))
                .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.title", Matchers.equalTo("Validation error")))
        .andExpect(jsonPath("$.detail", Matchers.containsString("Wrong email format")))
        .andExpect(jsonPath("$.detail", Matchers.containsString("Password must be")));
  }

  @Test
  void should_return401_when_login_wrongCredentials() throws Exception {
    when(userService.loginAndReturnCart(any(UserDto.class)))
        .thenThrow(WrongCredentialsException.class);

    mockMvc
        .perform(
            put("/users/login")
                .content(mapper.writeValueAsString(userDto))
                .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isUnauthorized())
        .andExpect(jsonPath("$.title", Matchers.equalTo("Wrong credentials")))
        .andExpect(jsonPath("$.detail", Matchers.containsString("Wrong credentials")));
  }

  @Test
  void should_return409_when_register_userAlreadyExists() throws Exception {
    doThrow(UserAlreadyExistsException.class).when(userService).register(any());

    mockMvc
        .perform(
            post("/users/register")
                .content(mapper.writeValueAsString(userDto))
                .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isConflict())
        .andExpect(jsonPath("$.title", Matchers.equalTo("Invalid user data")));
  }
}
