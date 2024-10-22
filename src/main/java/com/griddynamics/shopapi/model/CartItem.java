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
public class CartItem {

  @EmbeddedId
  @AttributeOverrides({
    @AttributeOverride(name = "productId", column = @Column(name = "product_id")),
    @AttributeOverride(name = "ordinal", column = @Column(name = "ordinal"))
  })
  private CartItemPrimaryKey id = new CartItemPrimaryKey();

  private int quantity;

  @ManyToOne(optional = false)
  @ToString.Exclude
  private Cart cart;

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

    CartItem cartItem = (CartItem) o;

    return Objects.equals(id, cartItem.id);
  }

  @Override
  public int hashCode() {
    return id != null ? id.hashCode() : 0;
  }
}
