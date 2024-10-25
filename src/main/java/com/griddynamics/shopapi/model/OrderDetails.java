package com.griddynamics.shopapi.model;

import jakarta.persistence.*;
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
  private double total = 0;

  @Column(nullable = false)
  @CreationTimestamp
  private LocalDateTime createdAt;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private OrderStatus status;

  @ManyToOne(optional = false)
  private Client client;

  @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<OrderItem> items = new LinkedList<>();

  public void addProduct(Product product, int quantity) {
    Optional<OrderItem> alreadyIn =
        items.stream().filter(item -> item.getProductId() == product.getId()).findAny();
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

    total += quantity * product.getPrice();
  }

  public int updateAndGetDifferenceInProductAmount(long productId, int newAmount) {
    Optional<OrderItem> itemOp =
        items.stream().filter(item -> item.getProductId() == productId).findAny();
    if (itemOp.isEmpty()) {
      return newAmount;
    }
    OrderItem item = itemOp.get();
    int diff = newAmount - item.getQuantity();
    total += diff * item.getPrice();
    item.setQuantity(newAmount);
    return diff;
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
    total -= item.getQuantity() * item.getPrice();
    items.remove(item);
    return item;
  }

  public void clearOrder() {
    items.clear();
    total = 0;
  }

  public void setClient(Client client) {
    if (this.client != null) {
      this.client.removeOrder(this);
    }

    client.addOrder(this);
    this.client = client;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    OrderDetails that = (OrderDetails) o;

    if (!Objects.equals(createdAt, that.createdAt)) return false;
    return Objects.equals(client, that.client);
  }

  @Override
  public int hashCode() {
    int result = createdAt != null ? createdAt.hashCode() : 0;
    result = 31 * result + (client != null ? client.hashCode() : 0);
    return result;
  }
}
