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
  private double total;

  @Column(nullable = false)
  @CreationTimestamp
  private LocalDateTime createdAt;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private OrderStatus status;

  @ManyToOne(optional = false)
  private Client client;

  @OneToMany(mappedBy = "order", cascade = CascadeType.ALL)
  private List<OrderItem> items = new LinkedList<>();

  public void addProduct(Product product, int quantity) {
    OrderItem item = new OrderItem();
    item.setOrder(this);
    item.setProduct(product);
    item.setQuantity(quantity);
    item.setOrdinal(items.size());
    item.setPrice(product.getPrice());

    items.add(item);

    total += quantity * product.getPrice();
  }

  public void removeProduct(Product product) {
    Optional<OrderItem> itemOp =
        items.stream().filter(item -> item.getProductId() == product.getId()).findAny();
    if (itemOp.isEmpty()) {
      return;
    }
    OrderItem item = itemOp.get();
    total -= item.getQuantity() * item.getPrice();
    items.remove(item);
  }

  public void clearOrder() {
    total = 0;
    items = new LinkedList<>();
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
