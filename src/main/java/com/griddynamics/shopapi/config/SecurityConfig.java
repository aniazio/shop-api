package com.griddynamics.shopapi.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

  @Bean
  public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    return http.authorizeHttpRequests(
            authorization ->
                authorization
                    .requestMatchers(
                        HttpMethod.GET,
                        "/",
                        "/products",
                        "/users/*/orders",
                        "/users/*/orders/*",
                        "/cart",
                        "/cart/items")
                    .permitAll()
                    .requestMatchers(
                        HttpMethod.POST,
                        "/users/login",
                        "/users/register",
                        "/users/reset",
                        "/cart/items")
                    .permitAll()
                    .requestMatchers(
                        HttpMethod.DELETE, "/users/*/orders/*", "/cart/items/*", "/cart")
                    .permitAll()
                    .requestMatchers(HttpMethod.PUT, "/users/reset", "/cart/checkout")
                    .permitAll()
                    .requestMatchers(HttpMethod.PATCH, "/cart/items")
                    .permitAll()
                    .requestMatchers("/h2-console/**")
                    .permitAll())
        .headers(h -> h.frameOptions(HeadersConfigurer.FrameOptionsConfig::sameOrigin))
        .csrf(AbstractHttpConfigurer::disable)
        .build();
  }
}
