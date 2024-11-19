package com.griddynamics.shopapi.dto;

import com.griddynamics.shopapi.model.User;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import java.util.Locale;
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

  @Email(message = "Wrong email format")
  private String email;

  @Pattern(
      regexp = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[!@#$%^&*()])[A-Za-z\\d!@#$%^&*()]{5,}$",
      message =
          "Password must be at least 5 characters long, with at least one letter, one digit, and one special character")
  private String password;

  public UserDto(User user) {
    id = user.getId();
    email = user.getEmail().toLowerCase(Locale.ROOT);
    password = null;
  }

  public void setEmail(String email) {
    this.email = email.toLowerCase(Locale.ROOT);
  }

  public String getEmail() {
    return email.toLowerCase(Locale.ROOT);
  }
}
