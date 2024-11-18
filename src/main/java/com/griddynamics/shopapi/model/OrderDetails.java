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
public class OrderDetails {
  @Id
  @SequenceGenerator(name = "seqOrder", sequenceName = "ORDERS_SEQ", allocationSize = 1)
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "seqOrder")
  private Long id;

  @Column(nullable = false)
  @PositiveOrZero
  private BigDecimal total = BigDecimal.valueOf(0);

  @Column(nullable = false)
  @CreationTimestamp
  private LocalDateTime createdAt;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private OrderStatus status;

  @ManyToOne(optional = false)
  @ToString.Exclude
  private User user;

  @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
  @OrderBy("addedAt ASC")
  private List<OrderItem> items = new LinkedList<>();

  public Optional<OrderItem> getItemByProductId(long productId) {
    return items.stream().filter(item -> item.getProductId() == productId).findAny();
  }

  public void addProduct(Product product, int quantity) {
    Optional<OrderItem> alreadyIn = getItemByProductId(product.getId());
    if (alreadyIn.isPresent()) {
      OrderItem item = alreadyIn.get();
      item.setQuantity(item.getQuantity() + quantity);
    } else {
      OrderItem item = new OrderItem();
      item.setOrder(this);
      item.setProduct(product);
      item.setQuantity(quantity);
      item.setPrice(product.getPrice());

      items.add(item);
    }
    total = total.add(product.getPrice().multiply(BigDecimal.valueOf(quantity)));
  }

  public void updateProductAmount(long productId, int newAmount) {
    Optional<OrderItem> itemOp =
        items.stream().filter(item -> item.getProductId() == productId).findAny();
    if (itemOp.isEmpty()) {
      return;
    }
    OrderItem item = itemOp.get();
    int diff = newAmount - item.getQuantity();
    total = total.add(item.getPrice().multiply(BigDecimal.valueOf(diff)));
    item.setQuantity(newAmount);
  }

  public void removeProduct(Product product) {
    removeProduct(product.getId());
  }

  public OrderItem removeProduct(long productId) {
    Optional<OrderItem> itemOp =
        items.stream().filter(item -> item.getProductId() == productId).findAny();
    if (itemOp.isEmpty()) {
      return null;
    }
    OrderItem item = itemOp.get();
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
      this.user.removeOrder(this);
    }

    user.addOrder(this);
    this.user = user;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    OrderDetails that = (OrderDetails) o;

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
