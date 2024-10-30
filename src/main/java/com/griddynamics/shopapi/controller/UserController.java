package com.griddynamics.shopapi.controller;

import com.griddynamics.shopapi.dto.CartDto;
import com.griddynamics.shopapi.dto.UserDto;
import com.griddynamics.shopapi.service.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("users")
public class UserController {

  private final UserService userService;

  public UserController(UserService userService) {
    this.userService = userService;
  }

  @PostMapping("/login")
  public ResponseEntity<Void> loginUser(@RequestBody UserDto userDto, HttpSession session) {
    CartDto cart = userService.loginAndReturnCart(userDto);

    session.setAttribute("cartId", cart.getId());
    session.setAttribute("userId", cart.getUserId());
    return new ResponseEntity<>(HttpStatus.OK);
  }

  @PostMapping("/register")
  public ResponseEntity<Void> registerUser(@RequestBody UserDto userDto) {
    userService.register(userDto);
    return new ResponseEntity<>(HttpStatus.CREATED);
  }

  @PostMapping("/reset")
  public ResponseEntity<String> requestPasswordReset(@RequestBody UserDto userDto) {
    userService.startResettingPassword(userDto);
    String msg = "Email with token was sent";

    return ResponseEntity.ok(msg);
  }

  @PutMapping("/reset")
  public ResponseEntity<Void> resetPassword(
      @RequestBody UserDto userDto, @RequestParam(name = "reset-token") String token) {
    userService.resetPassword(userDto, token);
    return new ResponseEntity<>(HttpStatus.OK);
  }
}
