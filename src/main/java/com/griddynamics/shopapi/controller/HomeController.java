package com.griddynamics.shopapi.controller;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.hateoas.Links;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
public class HomeController {

  @GetMapping
  public Links getHome(HttpSession session) {
    log.debug("HomeController.getHome; Request received for session id {}", session.getId());
    Links response =
        Links.of(linkTo(methodOn(ProductController.class).getAll()).withRel("products"))
            .and(
                linkTo(methodOn(UserController.class).loginUser(null, session))
                    .withRel("loginForm"));

    if (session.getAttribute("userId") != null) {
      return response
          .and(linkTo(methodOn(CartController.class).getCart(session)).withRel("cart"))
          .and(linkTo(methodOn(OrderController.class).getAllOrders(session)).withRel("orders"));
    }
    log.debug("HomeController.getHome; Response sent for session id {}", session.getId());
    return response;
  }
}
