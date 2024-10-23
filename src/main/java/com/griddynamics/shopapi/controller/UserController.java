package com.griddynamics.shopapi.controller;

import com.griddynamics.shopapi.dto.CartDto;
import com.griddynamics.shopapi.dto.ClientDto;
import com.griddynamics.shopapi.exceptions.UserNotFoundException;
import com.griddynamics.shopapi.service.UserService;
import jakarta.servlet.http.HttpSession;
import java.util.Optional;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("users")
public class UserController {

  private final UserService userService;

  public UserController(UserService userService) {
    this.userService = userService;
  }

  @PostMapping("/login")
  public void loginUser(@RequestBody ClientDto clientDto, HttpSession session) {
    Optional<CartDto> cart = userService.isProperUser(clientDto);
    session.setAttribute(
        "cartId",
        cart.orElseThrow(() -> new UserNotFoundException("Wrong credentials: " + clientDto))
            .getId());
    session.setAttribute("userId", clientDto.getId());
  }

  @ResponseStatus(HttpStatus.CREATED)
  @PostMapping("/register")
  public void registerUser(@RequestBody ClientDto clientDto) {
    userService.register(clientDto);
  }

  @PostMapping("/reset")
  public void resetPassword(@RequestBody ClientDto clientDto) {
    userService.resetPassword(clientDto);
  }
}
