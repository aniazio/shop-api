package com.griddynamics.shopapi.controller;

import com.griddynamics.shopapi.dto.CartDto;
import com.griddynamics.shopapi.dto.OrderDto;
import com.griddynamics.shopapi.dto.OrderItemDto;
import com.griddynamics.shopapi.exceptions.ForbiddenResourcesException;
import com.griddynamics.shopapi.service.CartService;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/cart")
public class CartController {
  private final CartService cartService;

  public CartController(CartService cartService) {
    this.cartService = cartService;
  }

  @GetMapping("")
  public CartDto getCart(HttpSession session) {
    long userId = getAttributeSafely("userId", session);
    return cartService.getCartFor(userId);
  }

  @DeleteMapping("{productId}")
  public void deleteItemFromCart(@PathVariable long productId, HttpSession session) {
    long cartId = getAttributeSafely("cartId", session);
    CartDto updatedCart = cartService.deleteItemFromCart(cartId, productId);
  }

  @PatchMapping("/update-item")
  public void updateItemAmount(@RequestBody OrderItemDto orderItemDto, HttpSession session) {
    long cartId = getAttributeSafely("cartId", session);
    CartDto updatedCart = cartService.updateItemAmount(orderItemDto, cartId);
  }

  @PutMapping("/checkout")
  public OrderDto checkout(HttpSession session) {
    long cartId = getAttributeSafely("cartId", session);
    long userId = getAttributeSafely("userId", session);
    OrderDto order = cartService.checkout(cartId);
    session.setAttribute("cartId", cartService.getIdOfNewCartFor(userId));
    return order;
  }

  @DeleteMapping("")
  public void clearCart(HttpSession session) {
    long cartId = getAttributeSafely("cartId", session);
    cartService.clearCart(cartId);
  }

  private long getAttributeSafely(String attribute, HttpSession session) {
    Object sessionAttribute = session.getAttribute(attribute);
    if (sessionAttribute == null) {
      throw new ForbiddenResourcesException(
          attribute + " not found in session for the method, which it requires.");
    }
    return (long) sessionAttribute;
  }
}
