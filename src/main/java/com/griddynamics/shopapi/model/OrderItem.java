package com.griddynamics.shopapi.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

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

  @Column(nullable = false)
  @CreationTimestamp
  private LocalDateTime addedAt;

  @MapsId("orderId")
  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "order_id", referencedColumnName = "id")
  @ToString.Exclude
  private OrderDetails order;

  public long getProductId() {
    return id.getProductId();
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
