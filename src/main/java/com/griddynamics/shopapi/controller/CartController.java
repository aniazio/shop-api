package com.griddynamics.shopapi.controller;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

import com.griddynamics.shopapi.dto.CartDto;
import com.griddynamics.shopapi.dto.OrderDto;
import com.griddynamics.shopapi.dto.OrderItemDto;
import com.griddynamics.shopapi.dto.SessionInfo;
import com.griddynamics.shopapi.exception.ForbiddenResourcesException;
import com.griddynamics.shopapi.exception.UnauthorizedException;
import com.griddynamics.shopapi.service.CartService;
import jakarta.servlet.http.HttpSession;
import java.net.URI;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/cart")
public class CartController {
  private final CartService cartService;

  public CartController(CartService cartService) {
    this.cartService = cartService;
  }

  @GetMapping("")
  public EntityModel<CartDto> getCart(HttpSession session) {
    SessionInfo sessionInfo = authorizeAndGetSessionInfo(session);
    CartDto returned = cartService.getCartFor(sessionInfo);

    EntityModel<CartDto> response = EntityModel.of(returned);
    response.add(linkTo(methodOn(this.getClass()).getItems(session)).withRel("items"));
    response.add(linkTo(methodOn(this.getClass()).checkout(session)).withRel("checkoutForm"));
    response.add(linkTo(methodOn(this.getClass()).getCart(session)).withSelfRel());
    return response;
  }

  @GetMapping("/items")
  public CollectionModel<OrderItemDto> getItems(HttpSession session) {
    SessionInfo sessionInfo = authorizeAndGetSessionInfo(session);
    CartDto returned = cartService.getCartFor(sessionInfo);

    CollectionModel<OrderItemDto> response = CollectionModel.of(returned.getItems());
    response.add(linkTo(methodOn(this.getClass()).getCart(session)).withRel("cart"));
    response.add(linkTo(methodOn(this.getClass()).getItems(session)).withSelfRel());
    return response;
  }

  @DeleteMapping("/items/{productId}")
  public void deleteItemFromCart(@PathVariable long productId, HttpSession session) {
    SessionInfo sessionInfo = authorizeAndGetSessionInfo(session);
    cartService.deleteItemFromCart(productId, sessionInfo);
  }

  @PostMapping("/items")
  public ResponseEntity<Void> addItemToCart(
      @RequestBody OrderItemDto orderItemDto, HttpSession session) {
    SessionInfo sessionInfo = authorizeAndGetSessionInfo(session);
    cartService.addItem(orderItemDto, sessionInfo);

    URI location = linkTo(methodOn(this.getClass()).getItems(session)).toUri();
    HttpHeaders responseHeaders = new HttpHeaders();
    responseHeaders.setLocation(location);
    return new ResponseEntity<>(null, responseHeaders, HttpStatus.CREATED);
  }

  @PatchMapping("/items")
  public ResponseEntity<Void> updateItemAmount(
      @RequestBody OrderItemDto orderItemDto, HttpSession session) {
    SessionInfo sessionInfo = authorizeAndGetSessionInfo(session);
    cartService.updateItemAmount(orderItemDto, sessionInfo);

    URI location = linkTo(methodOn(this.getClass()).getItems(session)).toUri();
    HttpHeaders responseHeaders = new HttpHeaders();
    responseHeaders.setLocation(location);
    return new ResponseEntity<>(null, responseHeaders, HttpStatus.OK);
  }

  @PutMapping("/checkout")
  public EntityModel<OrderDto> checkout(HttpSession session) {
    SessionInfo sessionInfo = authorizeAndGetSessionInfo(session);
    OrderDto order = cartService.checkout(sessionInfo);
    session.setAttribute("cartId", cartService.getIdOfNewCart(sessionInfo));

    EntityModel<OrderDto> response = EntityModel.of(order);
    response.add(linkTo(methodOn(this.getClass()).getCart(session)).withRel("newCart"));
    response.add(
        linkTo(
                methodOn(OrderController.class)
                    .getOrderFor(order.getUserId(), order.getId(), session))
            .withSelfRel());
    return response;
  }

  @DeleteMapping("")
  public ResponseEntity<Void> clearCart(HttpSession session) {
    SessionInfo sessionInfo = authorizeAndGetSessionInfo(session);
    cartService.clearCart(sessionInfo);

    URI location = linkTo(methodOn(this.getClass()).getCart(session)).toUri();
    HttpHeaders responseHeaders = new HttpHeaders();
    responseHeaders.setLocation(location);
    return new ResponseEntity<>(null, responseHeaders, HttpStatus.OK);
  }

  private SessionInfo authorizeAndGetSessionInfo(HttpSession session) {
    Object sessionUserId = session.getAttribute("userId");
    if (sessionUserId == null) {
      throw new UnauthorizedException();
    }
    Object sessionCartId = session.getAttribute("cartId");
    if (sessionCartId == null) {
      throw new ForbiddenResourcesException("cartId not found");
    }
    return new SessionInfo((long) sessionUserId, (long) sessionCartId);
  }
}
