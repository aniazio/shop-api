package com.griddynamics.shopapi.dto.validation;

import com.griddynamics.shopapi.dto.OrderItemDto;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class OrderItemDtoValidator implements ConstraintValidator<ValidOrderItemDto, OrderItemDto> {
  @Override
  public boolean isValid(
      OrderItemDto orderItemDto, ConstraintValidatorContext constraintValidatorContext) {
    Long productId = orderItemDto.getProductId();
    Integer id = orderItemDto.getOrdinal();
    if ((productId == null || productId < 0) && (id == null || id < 0)) {
      return false;
    }
    return true;
  }
}
