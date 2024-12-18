package com.griddynamics.shopapi.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.PositiveOrZero;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.CreationTimestamp;

@Entity
@Getter
@Setter
@NoArgsConstructor
@ToString
public class Cart {

  @Id private long id;

  @Column(nullable = false)
  @PositiveOrZero
  private BigDecimal total = BigDecimal.valueOf(0);

  @Column(nullable = false)
  @CreationTimestamp
  private LocalDateTime createdAt;

  @MapsId
  @OneToOne(optional = false)
  @ToString.Exclude
  private User user;

  @OneToMany(
      mappedBy = "cart",
      cascade = CascadeType.ALL,
      orphanRemoval = true,
      fetch = FetchType.EAGER)
  @OrderBy("addedAt ASC")
  private List<CartItem> items = new LinkedList<>();

  public Optional<CartItem> getItemByProductId(long productId) {
    return items.stream().filter(item -> item.getProductId() == productId).findAny();
  }

  public void addProduct(Product product, int quantity) {
    Optional<CartItem> alreadyIn = getItemByProductId(product.getId());
    if (alreadyIn.isPresent()) {
      CartItem item = alreadyIn.get();
      item.setQuantity(item.getQuantity() + quantity);
    } else {
      CartItem item = new CartItem();
      item.setCart(this);
      item.setProduct(product);
      item.setQuantity(quantity);
      item.setPrice(product.getPrice());

      items.add(item);
    }
    total = total.add(product.getPrice().multiply(BigDecimal.valueOf(quantity)));
  }

  public void updateProductAmount(long productId, int newAmount) {
    Optional<CartItem> itemOp = getItemByProductId(productId);
    if (itemOp.isEmpty()) {
      return;
    }
    CartItem item = itemOp.get();
    int diff = newAmount - item.getQuantity();
    total = total.add(item.getPrice().multiply(BigDecimal.valueOf(diff)));
    item.setQuantity(newAmount);
  }

  public CartItem removeProduct(long productId) {
    Optional<CartItem> itemOp = getItemByProductId(productId);
    if (itemOp.isEmpty()) {
      return null;
    }
    CartItem item = itemOp.get();
    total = total.subtract(item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())));
    items.remove(item);
    return item;
  }

  public void clearOrder() {
    items.clear();
    total = BigDecimal.valueOf(0);
  }

  public void setUser(User user) {
    if (this.user != null) {
      this.user.setCart(null);
    }

    user.setCart(this);
    this.user = user;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    Cart that = (Cart) o;

    if (!Objects.equals(createdAt, that.createdAt)) return false;
    return Objects.equals(user, that.user);
  }

  @Override
  public int hashCode() {
    int result = createdAt != null ? createdAt.hashCode() : 0;
    result = 31 * result + (user != null ? user.hashCode() : 0);
    return result;
  }
}
