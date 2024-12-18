package com.griddynamics.shopapi.controller;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

import com.griddynamics.shopapi.dto.*;
import com.griddynamics.shopapi.service.CartService;
import com.griddynamics.shopapi.service.SessionService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/cart")
@RequiredArgsConstructor
public class CartController {
  private final CartService cartService;
  private final SessionService sessionService;

  @GetMapping
  public EntityModel<CartDto> getCart(HttpSession session) {
    long userId = sessionService.getUserId(session);
    CartDto cartDto = cartService.getCartFor(userId);

    EntityModel<CartDto> response = EntityModel.of(cartDto);
    response.add(linkTo(methodOn(this.getClass()).getItems(session)).withRel("items"));
    response.add(linkTo(methodOn(this.getClass()).checkout(session)).withRel("checkoutForm"));
    response.add(linkTo(methodOn(this.getClass()).getCart(session)).withSelfRel());

    return response;
  }

  @GetMapping("/items")
  public CollectionModel<CartItemDto> getItems(HttpSession session) {
    long userId = sessionService.getUserId(session);
    CartDto cartDto = cartService.getCartFor(userId);
    List<CartItemDto> items = cartDto.getItems();

    CollectionModel<CartItemDto> response = CollectionModel.of(items);
    response.add(linkTo(methodOn(this.getClass()).getCart(session)).withRel("cart"));
    response.add(linkTo(methodOn(this.getClass()).getItems(session)).withSelfRel());

    return response;
  }

  @DeleteMapping("/items/{productId}")
  public void deleteItemFromCart(@PathVariable long productId, HttpSession session) {
    long userId = sessionService.getUserId(session);
    cartService.deleteItemFromCart(productId, userId);
  }

  @PostMapping("/items")
  public ResponseEntity<Void> addItemToCart(
      @RequestBody @Valid CartItemDto cartItemDto, HttpSession session) {
    long userId = sessionService.getUserId(session);
    cartService.addItem(cartItemDto, userId);

    URI location = linkTo(methodOn(this.getClass()).getItems(session)).toUri();
    HttpHeaders responseHeaders = new HttpHeaders();
    responseHeaders.setLocation(location);

    return new ResponseEntity<>(null, responseHeaders, HttpStatus.CREATED);
  }

  @PatchMapping("/items")
  public ResponseEntity<Void> updateItemAmount(
      @RequestBody @Valid CartItemDto cartItemDto, HttpSession session) {
    long userId = sessionService.getUserId(session);
    cartService.updateItemAmount(cartItemDto, userId);

    URI location = linkTo(methodOn(this.getClass()).getItems(session)).toUri();
    HttpHeaders responseHeaders = new HttpHeaders();
    responseHeaders.setLocation(location);

    return new ResponseEntity<>(null, responseHeaders, HttpStatus.OK);
  }

  @PutMapping("/checkout")
  public EntityModel<OrderDto> checkout(HttpSession session) {
    long userId = sessionService.getUserId(session);
    OrderDto orderDto = cartService.checkout(userId);
    cartService.createNewCart(userId);

    EntityModel<OrderDto> response = EntityModel.of(orderDto);
    response.add(linkTo(methodOn(this.getClass()).getCart(session)).withRel("newCart"));
    response.add(
        linkTo(methodOn(OrderController.class).getOrder(orderDto.getId(), session)).withSelfRel());

    return response;
  }

  @DeleteMapping
  public ResponseEntity<Void> clearCart(HttpSession session) {
    long userId = sessionService.getUserId(session);
    cartService.clearCart(userId);

    URI location = linkTo(methodOn(this.getClass()).getCart(session)).toUri();
    HttpHeaders responseHeaders = new HttpHeaders();
    responseHeaders.setLocation(location);

    return new ResponseEntity<>(null, responseHeaders, HttpStatus.OK);
  }


}
