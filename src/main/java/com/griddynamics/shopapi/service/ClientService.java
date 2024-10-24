package com.griddynamics.shopapi.service;

import com.griddynamics.shopapi.dto.CartDto;
import com.griddynamics.shopapi.dto.ClientDto;

public interface ClientService {

  void register(ClientDto clientDto);

  void resetPassword(ClientDto clientDto, String token);

  CartDto loginAndReturnCart(ClientDto clientDto);

  void startResettingPassword(ClientDto clientDto);
}
