package com.griddynamics.shopapi.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
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
  @JsonIgnore private String passwordNotEncoded;

  public ClientDto(Client client) {
    id = client.getId();
    email = client.getEmail();
    passwordNotEncoded = null;
  }
}
