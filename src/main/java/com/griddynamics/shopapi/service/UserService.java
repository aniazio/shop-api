package com.griddynamics.shopapi.service;

import com.griddynamics.shopapi.dto.CartDto;
import com.griddynamics.shopapi.dto.ClientDto;
import java.util.Optional;

public interface UserService {
  Optional<CartDto> isProperUser(ClientDto clientDto);

  void register(ClientDto clientDto);

  void resetPassword(ClientDto clientDto);
}
