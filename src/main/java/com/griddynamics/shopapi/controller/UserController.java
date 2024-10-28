package com.griddynamics.shopapi.controller;

import com.griddynamics.shopapi.dto.CartDto;
import com.griddynamics.shopapi.dto.UserDto;
import com.griddynamics.shopapi.service.UserService;
import jakarta.servlet.http.HttpSession;
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
  public void loginUser(@RequestBody UserDto userDto, HttpSession session) {
    CartDto cart = userService.loginAndReturnCart(userDto);

    session.setAttribute("cartId", cart.getId());
    session.setAttribute("userId", cart.getUserId());
  }

  @ResponseStatus(HttpStatus.CREATED)
  @PostMapping("/register")
  public void registerUser(@RequestBody UserDto userDto) {
    userService.register(userDto);
  }

  @PostMapping("/reset")
  public String requestPasswordReset(@RequestBody UserDto userDto) {
    userService.startResettingPassword(userDto);
    return "Email with token was sent";
  }

  @PutMapping("/reset")
  public void resetPassword(
      @RequestBody UserDto userDto, @RequestParam(name = "reset-token") String token) {
    userService.resetPassword(userDto, token);
  }
}
