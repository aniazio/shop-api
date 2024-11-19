package com.griddynamics.shopapi.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SessionInfo {

  @JsonProperty(access = JsonProperty.Access.READ_ONLY)
  private String sessionId;

  @JsonProperty(access = JsonProperty.Access.READ_ONLY)
  private Long userId;

  public SessionInfo(Long userId) {
    this.userId = userId;
  }
}
