package com.griddynamics.shopapi.dto.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Constraint(validatedBy = CartItemDtoValidator.class)
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidCartItemDto {

  String message() default "Id or productId required";

  Class<?>[] groups() default {};

  Class<? extends Payload>[] payload() default {};
}
