package com.griddynamics.shopapi.controller;

import com.griddynamics.shopapi.dto.CartDto;
import com.griddynamics.shopapi.dto.SessionInfo;
import com.griddynamics.shopapi.dto.UserDto;
import com.griddynamics.shopapi.service.UserService;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

  private final UserService userService;
  private final String RATE_LIMITER_NAME = "userController";

  @PutMapping("/login")
  @RateLimiter(name = RATE_LIMITER_NAME)
  public ResponseEntity<SessionInfo> loginUser(@RequestBody UserDto userDto, HttpSession session) {
    CartDto cartDto = userService.loginAndReturnCart(userDto);

    session.setAttribute("userId", cartDto.getUserId());

    SessionInfo response = new SessionInfo(session.getId(), cartDto.getUserId());
    return new ResponseEntity<>(response, HttpStatus.OK);
  }

  @PostMapping("/register")
  @RateLimiter(name = RATE_LIMITER_NAME)
  public ResponseEntity<Void> registerUser(@RequestBody @Valid UserDto userDto) {
    userService.register(userDto);
    return new ResponseEntity<>(HttpStatus.CREATED);
  }

  @PatchMapping("/reset")
  @RateLimiter(name = RATE_LIMITER_NAME)
  public ResponseEntity<String> requestPasswordReset(@RequestBody UserDto userDto) {
    String msg = userService.startResettingPassword(userDto);
    return ResponseEntity.ok(msg);
  }

  @PutMapping("/reset")
  @RateLimiter(name = RATE_LIMITER_NAME)
  public ResponseEntity<Void> resetPassword(
      @RequestBody @Valid UserDto userDto, @RequestParam(name = "reset-token") String token) {
    userService.resetPassword(userDto, token);
    return new ResponseEntity<>(HttpStatus.OK);
  }
}
