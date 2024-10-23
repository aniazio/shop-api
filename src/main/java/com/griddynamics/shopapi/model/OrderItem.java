package com.griddynamics.shopapi.model;

import jakarta.persistence.*;
import java.util.Objects;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Getter
@Setter
@NoArgsConstructor
@ToString
public class OrderItem {

  @EmbeddedId
  @AttributeOverrides({
    @AttributeOverride(name = "productId", column = @Column(name = "product_id")),
    @AttributeOverride(name = "ordinal", column = @Column(name = "ordinal"))
  })
  private OrderItemPrimaryKey id = new OrderItemPrimaryKey();

  @Column(nullable = false)
  private int quantity;

  @Column(nullable = false)
  private double price;

  @ManyToOne(optional = false)
  @ToString.Exclude
  private OrderDetails order;

  public int getOrdinal() {
    return id.getOrdinal();
  }

  public long getProductId() {
    return id.getProductId();
  }

  public void setOrdinal(int ordinal) {
    this.id.setOrdinal(ordinal);
  }

  public void setProduct(Product product) {
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
