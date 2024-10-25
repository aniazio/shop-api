package com.griddynamics.shopapi.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

  @Bean
  public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    return http.authorizeHttpRequests(authorization -> authorization.anyRequest().permitAll())
        .headers(h -> h.frameOptions(HeadersConfigurer.FrameOptionsConfig::sameOrigin))
        .csrf(AbstractHttpConfigurer::disable)
        .build();
  }
}
