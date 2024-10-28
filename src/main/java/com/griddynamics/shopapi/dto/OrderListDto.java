package com.griddynamics.shopapi.dto;

import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
public class OrderListDto {

  List<OrderDto> orders = new ArrayList<>();

  public void addOrder(OrderDto order) {
    orders.add(order);
  }

  public void removeOrder(OrderDto order) {
    orders.remove(order);
  }
}