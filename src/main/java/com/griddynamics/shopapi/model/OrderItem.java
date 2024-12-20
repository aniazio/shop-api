package com.griddynamics.shopapi.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.util.Objects;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@ToString
public class OrderItem {

  @EmbeddedId
  @AttributeOverrides({
    @AttributeOverride(name = "productId", column = @Column(name = "product_id")),
    @AttributeOverride(name = "orderId", column = @Column(name = "order_id"))
  })
  private OrderItemPrimaryKey id = new OrderItemPrimaryKey();

  @Column(nullable = false)
  @Positive
  private int quantity;

  @Column(nullable = false)
  private BigDecimal price;

  @MapsId("orderId")
  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "order_id", referencedColumnName = "id")
  @ToString.Exclude
  private OrderDetails order;

  @MapsId("productId")
  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "product_id", referencedColumnName = "id")
  @ToString.Exclude
  private Product product;

  public OrderItem(CartItem cartItem) {
    this.setProduct(cartItem.getProduct());
    price = cartItem.getPrice();
    quantity = cartItem.getQuantity();
  }

  public long getProductId() {
    return id.getProductId();
  }

  public void setProduct(Product product) {
    this.product = product;
    this.id.setProductId(product.getId());
  }


  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    OrderItem orderItem = (OrderItem) o;

    return Objects.equals(id, orderItem.id);
  }

  @Override
  public int hashCode() {
    return id != null ? id.hashCode() : 0;
  }
}
