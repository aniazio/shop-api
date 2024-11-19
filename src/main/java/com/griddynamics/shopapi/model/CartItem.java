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
public class CartItem {

  @EmbeddedId
  @AttributeOverrides({
    @AttributeOverride(name = "productId", column = @Column(name = "product_id")),
    @AttributeOverride(name = "cartId", column = @Column(name = "user_id"))
  })
  private CartItemPrimaryKey id = new CartItemPrimaryKey();

  @Column(nullable = false)
  @Positive
  private int quantity;

  @Column(nullable = false)
  private BigDecimal price;

  @Column(nullable = false)
  @CreationTimestamp
  private LocalDateTime addedAt;

  @MapsId("cartId")
  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "user_id", referencedColumnName = "user_id")
  @ToString.Exclude
  private Cart cart;

  @MapsId("productId")
  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "product_id", referencedColumnName = "id")
  @ToString.Exclude
  private Product product;

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

    CartItem orderItem = (CartItem) o;

    return Objects.equals(id, orderItem.id);
  }

  @Override
  public int hashCode() {
    return id != null ? id.hashCode() : 0;
  }
}
