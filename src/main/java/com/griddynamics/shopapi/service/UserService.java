package com.griddynamics.shopapi.service;

import com.griddynamics.shopapi.dto.CartDto;
import com.griddynamics.shopapi.dto.UserDto;

public interface UserService {

  void register(UserDto userDto);

  void resetPassword(UserDto userDto, String token);

  CartDto loginAndReturnCart(UserDto userDto);

  String startResettingPassword(UserDto userDto);
}
