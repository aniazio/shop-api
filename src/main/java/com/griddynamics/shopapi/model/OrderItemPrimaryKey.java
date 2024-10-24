package com.griddynamics.shopapi.model;

import jakarta.persistence.Embeddable;
import lombok.*;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@ToString
@EqualsAndHashCode
public class OrderItemPrimaryKey {

  private Long productId;
  private Long orderId;
}
