package com.griddynamics.shopapi.dto;

import com.griddynamics.shopapi.model.User;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@NoArgsConstructor
@ToString
public class UserDto {
  private Long id;
  private String email;
  private String password;

  public UserDto(User user) {
    id = user.getId();
    email = user.getEmail();
    password = null;
  }
}
