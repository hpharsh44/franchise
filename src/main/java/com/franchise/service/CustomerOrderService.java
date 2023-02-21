package com.franchise.service;

import com.franchise.entity.Order;
import com.franchise.entity.OrderItem;
import com.franchise.exceptions.InternalServerException;
import com.franchise.exceptions.InvalidRequestException;
import com.franchise.model.CustomerOrder;
import com.franchise.model.CustomerOrderItem;
import com.franchise.repository.OrderRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CustomerOrderService {

  private final OrderRepository orderRepository;

  public static List<CustomerOrderItem> getCustomerOrderItem(Set<OrderItem> orderItems) {
    try {
      log.info("Get Customer Order Item Invoked");
      List<CustomerOrderItem> customerOrderItems =
          orderItems.stream().map(orderItem -> mapToCustomerOrderItem(orderItem)).toList();
      log.info("Customer Order Item Received");
      return customerOrderItems;
    } catch (Exception e) {
      log.error("Error while getting customer order");
      throw new InternalServerException("Unknown exception.");
    }
  }

  public static CustomerOrderItem mapToCustomerOrderItem(OrderItem orderItem) {
    try {
      log.info("Mapping To Customer Order Item");
      CustomerOrderItem customerOrderItem =
          CustomerOrderItem.builder()
              .id(orderItem.getId())
              .menuItemId(orderItem.getMenuItemId())
              .price(orderItem.getPrice())
              .quntity(orderItem.getQuantity())
              .build();
      log.info("Customer Order Item Mapped");
      return customerOrderItem;

    } catch (Exception e) {
      log.error("Error while mapping to customer order item", e);
      throw new InternalServerException("Unknown exception.");
    }
  }

  public List<CustomerOrder> getAllCustomerOrder(Long customerId) {
    log.info("Getting All Customer Order");
    if (customerId == null) {
      log.error("Customer Id is null in request.");
      throw new InvalidRequestException("CustomerId is missing in input.");
    }

      List<Order> orders = orderRepository.findAllByCustomerId(customerId);
      List<CustomerOrder> customerOrders =
          orders.stream().map(order -> convertToCustomerOrder(order)).collect(Collectors.toList());
      log.info("Customer Order get Successfully");
      return customerOrders;
  }

  private CustomerOrder convertToCustomerOrder(Order order) {

    return CustomerOrder.builder()
        .id(order.getId())
        .franchiseId(order.getFranchise().getId())
        .customerId(order.getCustomerId())
        .menuId(order.getMenuId())
        .orderItems(getCustomerOrderItem(order.getOrderItems()))
        .totalAmount(order.getTotal())
        .pickUpTime(order.getPickUpTime().atOffset(ZoneOffset.UTC))
        .orderStatus(order.getStatus())
        .orderType(order.getOrderType())
        .build();
  }

  public CustomerOrder getCustomerOrderByOrderId(Long customerId, Long orderId) {
    if (customerId == null || orderId == null) {
      log.error("customerId or orderId is Null");
      throw new InvalidRequestException("CustomerId or OrderId is missing in input.");
    }
    log.info("getting customer order by Customer and order id");
    Order order = orderRepository.findByCustomerIdAndId(customerId, orderId);
    CustomerOrder customerOrder = convertToCustomerOrder(order);
    if (order.getOrderType() == CustomerOrder.OrderTypeEnum.TAKE_AWAY) {
        log.info("Customer order type is TAKE_AWAY");
      customerOrder.setMessage(getMessage(order.getPickUpTime().atOffset(ZoneOffset.UTC)));
    }
    log.info("Successfully get customer order by Customer and order id");
    return customerOrder;
  }

  public String getMessage(OffsetDateTime pickUpTime) {
    List<Order> pendingOrder =
        orderRepository.findAllByStatusInOrderByUpdatedAtDesc(
            Arrays.asList(
                CustomerOrder.OrderStatusEnum.PROCESSING, CustomerOrder.OrderStatusEnum.QUEUED));
    List<LocalDateTime> pendingOrderTime =
        pendingOrder.stream()
            .map(or -> or.getUpdatedAt().toLocalDateTime())
            .collect(Collectors.toList());
    Optional<LocalDateTime> minTime = pendingOrderTime.stream().min(LocalDateTime::compareTo);
    Optional<LocalDateTime> maxTime = pendingOrderTime.stream().min(LocalDateTime::compareTo);
    if (maxTime.isPresent() && pickUpTime.isAfter(maxTime.get().plusMinutes(10).atOffset(ZoneOffset.UTC))) {
      log.info("Order is available on chosen time.");
      return "Your order is In Queue will move shortly in InProgress..."
          + "You can pick your Order on your chosen time";
    } else if ((maxTime.isPresent() && pickUpTime.equals(maxTime.get())
        || (minTime.isPresent() && pickUpTime.equals(minTime.get())))
        || (minTime.isPresent() && pickUpTime.isBefore(minTime.get().atOffset(ZoneOffset.UTC)))) {
      log.info("Order is not available on chosen time.");
      return "Your order is In Queue will move shortly in InProgress..."
          + "Your Queue number is "
          + pendingOrder.size()
          + 1;
    }
    return null;
  }
}
