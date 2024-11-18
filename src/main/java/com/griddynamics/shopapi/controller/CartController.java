package com.griddynamics.shopapi.controller;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

import com.griddynamics.shopapi.dto.CartDto;
import com.griddynamics.shopapi.dto.OrderDto;
import com.griddynamics.shopapi.dto.OrderItemDto;
import com.griddynamics.shopapi.dto.SessionInfo;
import com.griddynamics.shopapi.service.CartService;
import com.griddynamics.shopapi.service.SessionService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/cart")
@Slf4j
@RequiredArgsConstructor
public class CartController {
  private final CartService cartService;
  private final SessionService sessionService;

  @GetMapping
  public EntityModel<CartDto> getCart(HttpSession session) {
    log.debug("CartController.getCart; Request received for session id {}", session.getId());
    SessionInfo sessionInfo = sessionService.authorizeAndGetSessionInfo(session);
    CartDto cartDto = cartService.getCartFor(sessionInfo);

    EntityModel<CartDto> response = EntityModel.of(cartDto);
    response.add(linkTo(methodOn(this.getClass()).getItems(session)).withRel("items"));
    response.add(linkTo(methodOn(this.getClass()).checkout(session)).withRel("checkoutForm"));
    response.add(linkTo(methodOn(this.getClass()).getCart(session)).withSelfRel());

    log.debug(
        "CartController.getCart; Response sent for session id {}: {}", session.getId(), cartDto);
    return response;
  }

  @GetMapping("/items")
  public CollectionModel<OrderItemDto> getItems(HttpSession session) {
    log.debug("CartController.getItems; Request received for session id {}", session.getId());
    SessionInfo sessionInfo = sessionService.authorizeAndGetSessionInfo(session);
    CartDto cartDto = cartService.getCartFor(sessionInfo);
    List<OrderItemDto> items = cartDto.getItems();

    CollectionModel<OrderItemDto> response = CollectionModel.of(items);
    response.add(linkTo(methodOn(this.getClass()).getCart(session)).withRel("cart"));
    response.add(linkTo(methodOn(this.getClass()).getItems(session)).withSelfRel());

    log.debug(
        "CartController.getItems; Response sent for session id {}: {}", session.getId(), items);
    return response;
  }

  @DeleteMapping("/items/{productId}")
  public void deleteItemFromCart(@PathVariable long productId, HttpSession session) {
    log.debug(
        "CartController.deleteItemFromCart; Request received for session id {}: product id = {}",
        session.getId(),
        productId);
    SessionInfo sessionInfo = sessionService.authorizeAndGetSessionInfo(session);
    cartService.deleteItemFromCart(productId, sessionInfo);

    log.debug(
        "CartController.deleteItemFromCart; Response sent for session id {}", session.getId());
  }

  @PostMapping("/items")
  public ResponseEntity<Void> addItemToCart(
      @RequestBody @Valid OrderItemDto orderItemDto, HttpSession session) {
    log.debug(
        "CartController.addItemToCart; Request received for session id {}: body = {}",
        session.getId(),
        orderItemDto);
    SessionInfo sessionInfo = sessionService.authorizeAndGetSessionInfo(session);
    cartService.addItem(orderItemDto, sessionInfo);

    URI location = linkTo(methodOn(this.getClass()).getItems(session)).toUri();
    HttpHeaders responseHeaders = new HttpHeaders();
    responseHeaders.setLocation(location);

    log.debug("CartController.addItemToCart; Response sent for session id {}", session.getId());
    return new ResponseEntity<>(null, responseHeaders, HttpStatus.CREATED);
  }

  @PatchMapping("/items")
  public ResponseEntity<Void> updateItemAmount(
      @RequestBody @Valid OrderItemDto orderItemDto, HttpSession session) {
    log.debug(
        "CartController.updateItemAmount; Request received for session id {}: body = {}",
        session.getId(),
        orderItemDto);
    SessionInfo sessionInfo = sessionService.authorizeAndGetSessionInfo(session);
    cartService.updateItemAmount(orderItemDto, sessionInfo);

    URI location = linkTo(methodOn(this.getClass()).getItems(session)).toUri();
    HttpHeaders responseHeaders = new HttpHeaders();
    responseHeaders.setLocation(location);

    log.debug("CartController.updateItemAmount; Response sent for session id {}", session.getId());
    return new ResponseEntity<>(null, responseHeaders, HttpStatus.OK);
  }

  @PutMapping("/checkout")
  public EntityModel<OrderDto> checkout(HttpSession session) {
    log.debug("CartController.checkout; Request received for session id {}", session.getId());
    SessionInfo sessionInfo = sessionService.authorizeAndGetSessionInfo(session);
    OrderDto orderDto = cartService.checkout(sessionInfo);
    session.setAttribute("cartId", cartService.getIdOfNewCart(sessionInfo));

    EntityModel<OrderDto> response = EntityModel.of(orderDto);
    response.add(linkTo(methodOn(this.getClass()).getCart(session)).withRel("newCart"));
    response.add(
        linkTo(methodOn(OrderController.class).getOrder(orderDto.getId(), session)).withSelfRel());

    log.debug(
        "CartController.checkout; Response sent for session id {}: {}", session.getId(), orderDto);
    return response;
  }

  @DeleteMapping
  public ResponseEntity<Void> clearCart(HttpSession session) {
    log.debug("CartController.clearCart; Request received for session id {}", session.getId());
    SessionInfo sessionInfo = sessionService.authorizeAndGetSessionInfo(session);
    cartService.clearCart(sessionInfo);

    URI location = linkTo(methodOn(this.getClass()).getCart(session)).toUri();
    HttpHeaders responseHeaders = new HttpHeaders();
    responseHeaders.setLocation(location);

    log.debug("CartController.clearCart; Response sent for session id {}", session.getId());
    return new ResponseEntity<>(null, responseHeaders, HttpStatus.OK);
  }


}
