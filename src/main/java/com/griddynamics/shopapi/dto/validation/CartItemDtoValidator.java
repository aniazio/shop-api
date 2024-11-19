package com.griddynamics.shopapi.dto.validation;

import com.griddynamics.shopapi.dto.CartItemDto;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class CartItemDtoValidator implements ConstraintValidator<ValidCartItemDto, CartItemDto> {
  @Override
  public boolean isValid(
      CartItemDto cartItemDto, ConstraintValidatorContext constraintValidatorContext) {
    Long productId = cartItemDto.getProductId();
    Integer id = cartItemDto.getOrdinal();
    if ((productId == null || productId < 0) && (id == null || id < 0)) {
      return false;
    }
    return true;
  }
}
