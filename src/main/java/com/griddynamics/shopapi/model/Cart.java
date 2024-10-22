package com.griddynamics.shopapi.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
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
  @Id
  @SequenceGenerator(name = "seqCart", sequenceName = "CARTS_SEQ", allocationSize = 1)
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "seqCart")
  private Long id;

  @Column(nullable = false)
  private double total;

  @OneToMany(mappedBy = "cart", cascade = CascadeType.ALL)
  private Set<CartItem> items = new HashSet<>();

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  private Session session;

  @OneToOne(mappedBy = "cart")
  @ToString.Exclude
  private OrderDetails orderDetails;

  @Column(nullable = false)
  @CreationTimestamp
  private LocalDateTime createdAt;

  public void addProduct(Product product, int quantity) {
    CartItem item = new CartItem();
    item.setCart(this);
    item.setProduct(product);
    item.setQuantity(quantity);
    item.setOrdinal(items.size());

    items.add(item);

    total += quantity * product.getPrice();
  }

  public void removeProduct(Product product) {
    Optional<CartItem> itemOp =
        items.stream().filter(item -> item.getProductId() == product.getId()).findAny();
    if (itemOp.isEmpty()) {
      return;
    }
    CartItem item = itemOp.get();
    total -= item.getQuantity() * product.getPrice();
    items.remove(item);
  }

  public void clearCart() {
    total = 0;
    items = new HashSet<>();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    Cart cart = (Cart) o;

    if (!Objects.equals(session, cart.session)) return false;
    return Objects.equals(createdAt, cart.createdAt);
  }

  @Override
  public int hashCode() {
    int result = session != null ? session.hashCode() : 0;
    result = 31 * result + (createdAt != null ? createdAt.hashCode() : 0);
    return result;
  }
}
