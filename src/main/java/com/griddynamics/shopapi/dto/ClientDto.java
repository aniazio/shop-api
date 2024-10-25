package com.griddynamics.shopapi.dto;

import com.griddynamics.shopapi.model.Client;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@NoArgsConstructor
@ToString
public class ClientDto {
  private Long id;
  private String email;
  private String password;

  public ClientDto(Client client) {
    id = client.getId();
    email = client.getEmail();
    password = null;
  }
}
