package com.griddynamics.shopapi.controller;

import com.griddynamics.shopapi.dto.CartDto;
import com.griddynamics.shopapi.dto.SessionInfo;
import com.griddynamics.shopapi.dto.UserDto;
import com.griddynamics.shopapi.service.UserService;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
@Slf4j
@RequiredArgsConstructor
public class UserController {

  private final UserService userService;
  private final String RATE_LIMITER_NAME = "userController";

  @PutMapping("/login")
  @RateLimiter(name = RATE_LIMITER_NAME)
  public ResponseEntity<SessionInfo> loginUser(@RequestBody UserDto userDto, HttpSession session) {
    log.debug(
        "UserController.loginUser; Request received for session id {}; body = {}",
        session.getId(),
        userDto);
    CartDto cartDto = userService.loginAndReturnCart(userDto);

    session.setAttribute("cartId", cartDto.getId());
    session.setAttribute("userId", cartDto.getUserId());

    SessionInfo response = new SessionInfo(session.getId(), cartDto.getUserId(), cartDto.getId());
    log.debug("UserController.loginUser: Response sent for session id {}", session.getId());
    return new ResponseEntity<>(response, HttpStatus.OK);
  }

  @PostMapping("/register")
  @RateLimiter(name = RATE_LIMITER_NAME)
  public ResponseEntity<Void> registerUser(@RequestBody @Valid UserDto userDto) {
    log.debug("UserController.registerUser; Request received; body = {}", userDto);
    userService.register(userDto);
    log.debug("UserController.registerUser: Response sent");
    return new ResponseEntity<>(HttpStatus.CREATED);
  }

  @PatchMapping("/reset")
  @RateLimiter(name = RATE_LIMITER_NAME)
  public ResponseEntity<String> requestPasswordReset(@RequestBody UserDto userDto) {
    log.debug("UserController.requestPasswordReset; Request received; body = {}", userDto);
    String msg = userService.startResettingPassword(userDto);
    log.debug("UserController.requestPasswordReset: Response sent");
    return ResponseEntity.ok(msg);
  }

  @PutMapping("/reset")
  @RateLimiter(name = RATE_LIMITER_NAME)
  public ResponseEntity<Void> resetPassword(
      @RequestBody @Valid UserDto userDto, @RequestParam(name = "reset-token") String token) {
    log.debug(
        "UserController.resetPassword; Request received; reset-token = {}, body = {}",
        token,
        userDto);
    userService.resetPassword(userDto, token);
    log.debug("UserController.resetPassword: Response sent");
    return new ResponseEntity<>(HttpStatus.OK);
  }
}
