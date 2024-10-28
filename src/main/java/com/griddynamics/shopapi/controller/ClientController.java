package com.griddynamics.shopapi.controller;

import com.griddynamics.shopapi.dto.CartDto;
import com.griddynamics.shopapi.dto.ClientDto;
import com.griddynamics.shopapi.service.ClientService;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("clients")
public class ClientController {

  private final ClientService clientService;

  public ClientController(ClientService clientService) {
    this.clientService = clientService;
  }

  @PostMapping("/login")
  public void loginUser(@RequestBody ClientDto clientDto, HttpSession session) {
    CartDto cart = clientService.loginAndReturnCart(clientDto);

    session.setAttribute("cartId", cart.getId());
    session.setAttribute("clientId", cart.getClientId());
  }

  @ResponseStatus(HttpStatus.CREATED)
  @PostMapping("/register")
  public void registerUser(@RequestBody ClientDto clientDto) {
    clientService.register(clientDto);
  }

  @PostMapping("/reset")
  public String requestPasswordReset(@RequestBody ClientDto clientDto) {
    clientService.startResettingPassword(clientDto);
    return "Email with token was sent";
  }

  @PutMapping("/reset")
  public void resetPassword(
      @RequestBody ClientDto clientDto, @RequestHeader("Reset-token") String token) {
    clientService.resetPassword(clientDto, token);
  }
}
