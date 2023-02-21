package com.franchise.service;

import com.franchise.entity.Franchise;
import com.franchise.entity.MenuItem;
import com.franchise.entity.Order;
import com.franchise.entity.OrderItem;
import com.franchise.exceptions.InternalServerException;
import com.franchise.exceptions.InvalidRequestException;
import com.franchise.exceptions.ResourceNotFoundException;
import com.franchise.model.CustomerOrder;
import com.franchise.model.OrderRequest;
import com.franchise.model.UpdateOrderStatusRequest;
import com.franchise.repository.FranchiseRepository;
import com.franchise.repository.MenuItemRepository;
import com.franchise.repository.OrderItemRepository;
import com.franchise.repository.OrderRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class FranchiseOrderService {

  private final OrderRepository orderRepository;
  private final FranchiseRepository franchiseRepository;
  private final OrderItemRepository orderItemRepository;
  private final MenuItemRepository menuItemRepository;

  public CustomerOrder placeOrder(OrderRequest request) {
    log.info("Placing Order");
    validateOrderType(request);
    Franchise franchise =
        franchiseRepository
            .findById(request.getFranchiseId())
            .orElseThrow(
                () ->
                    new ResourceNotFoundException(
                        "Franchise not found by id: " + request.getFranchiseId()));
    log.info("Franchise found.");
    Map<Long, BigDecimal> menuItemMap = getFranchiseMenuItem(request.getMenuId());
    log.info("MenuItem found.");
    log.info("checking for pending orders");
    List<Order> pendingOrder =
        orderRepository.findAllByStatusInOrderByUpdatedAtDesc(
            Arrays.asList(
                CustomerOrder.OrderStatusEnum.PROCESSING, CustomerOrder.OrderStatusEnum.QUEUED));
    Order order = mapToFranchiseOrder(request, franchise, menuItemMap);
    Set<OrderItem> orderItems =
        mapToOrderItems(request.getMenuItemAndQuantityMap(), order, menuItemMap);
    order.setOrderItems(orderItems);

    BigDecimal total =
        orderItems.stream()
            .map(
                orderItem -> orderItem.getPrice().multiply(new BigDecimal(orderItem.getQuantity())))
            .reduce(BigDecimal.ZERO, (a, b) -> a.add(b));
    order.setTotal(total);
    orderRepository.save(order);

    log.info("Order is created");
    CustomerOrder customerOrder =
        CustomerOrder.builder()
            .id(order.getId())
            .customerId(order.getCustomerId())
            .franchiseId(order.getFranchise().getId())
            .menuId(order.getMenuId())
            .orderItems(CustomerOrderService.getCustomerOrderItem(order.getOrderItems()))
            .orderType(order.getOrderType())
            .orderStatus(order.getStatus())
            .pickUpTime(order.getPickUpTime().atOffset(ZoneOffset.UTC))
            .totalAmount(order.getTotal())
            .build();
    if (pendingOrder.isEmpty()) {
      log.info("No pending order is present");
      customerOrder.setMessage("congratulations your order is InProgress...");
    } else {
      log.info("Pending order is present update message accordingly.");
      if (request.getOrderType() == OrderRequest.OrderTypeEnum.TAKE_AWAY)
        updateMessageIfOrderTypeIsTakeAway(request, pendingOrder, customerOrder);
    }
    return customerOrder;
  }

  private void updateMessageIfOrderTypeIsTakeAway(
      OrderRequest request, List<Order> pendingOrder, CustomerOrder customerOrderDTO) {
    List<LocalDateTime> pendingOrderTime =
        pendingOrder.stream()
            .map(or -> or.getUpdatedAt().toLocalDateTime())
            .collect(Collectors.toList());
    Optional<LocalDateTime> minTime = pendingOrderTime.stream().min(LocalDateTime::compareTo);
    Optional<LocalDateTime> maxTime = pendingOrderTime.stream().min(LocalDateTime::compareTo);
    if (request.getPickUpTime().isAfter(maxTime.get().plusMinutes(10).atOffset(ZoneOffset.UTC))) {
      log.info("Order is available on chosen time.");
      customerOrderDTO.setMessage(
          "Your order is In Queue will move shortly in InProgress..."
              + "You can pick your Order on your chosen time");
    } else if (request.getPickUpTime().equals(maxTime.get())
        || request.getPickUpTime().equals(minTime.get())
        || request.getPickUpTime().isBefore(minTime.get().atOffset(ZoneOffset.UTC))) {
      log.info("Order is not available on chosen time.");
      customerOrderDTO.setMessage(
          "Your order is In Queue will move shortly in InProgress..."
              + "Your Queue number is "
              + pendingOrder.size()
              + 1);
    }
  }

  private void validateOrderType(OrderRequest request) {
    if (request.getOrderType() == OrderRequest.OrderTypeEnum.TAKE_AWAY
        && request.getPickUpTime() == null) {
      log.error("OrderType is TAKE_AWAY but pickUpTime is missing in request");
      throw new InvalidRequestException("OrderType TAKE_AWAY must require PickUpTime.");
    }
  }

  private Map<Long, BigDecimal> getFranchiseMenuItem(Long menuId) {
    try {
      log.info("Request for get menu.");
      List<MenuItem> menuItems = menuItemRepository.findAllByMenuId(menuId);
      if (menuItems.isEmpty()) {
        throw new ResourceNotFoundException("No items founds by menuId: " + menuId);
      }
      log.info("Menu found.");
      Map<Long, BigDecimal> menuItemPriceMap =
          menuItems.stream().collect(Collectors.toMap(MenuItem::getId, MenuItem::getPrice));
      return menuItemPriceMap;
    } catch (Exception e) {
      log.error("Exception while fetching Menu.");
      throw new InvalidRequestException("Exception while fetching Menu.");
    }
  }

  private Order mapToFranchiseOrder(
      OrderRequest customerOrder,
      Franchise franchise,
      Map<Long, BigDecimal> menuItemPriceDetailsMap) {
    Order order =
        Order.builder()
            .franchise(franchise)
            .menuId(customerOrder.getMenuId())
            .customerId(customerOrder.getCustomerId())
            .status(CustomerOrder.OrderStatusEnum.QUEUED)
            .orderType(
                CustomerOrder.OrderTypeEnum.fromValue(customerOrder.getOrderType().getValue()))
            .pickUpTime(customerOrder.getPickUpTime().toLocalDateTime())
            .build();
    return order;
  }

  private Set<OrderItem> mapToOrderItems(
      Map<String, BigDecimal> selectedMenuItemAndQuantityMap,
      Order order,
      Map<Long, BigDecimal> menuItemPriceDetailsMap) {
    try {
      Set<OrderItem> orderItems = new HashSet<>();
      for (Map.Entry<String, BigDecimal> menuItemAndQuantity :
          selectedMenuItemAndQuantityMap.entrySet()) {
        Long menuItemId = Long.valueOf(menuItemAndQuantity.getKey());
        BigDecimal quantity = menuItemAndQuantity.getValue();
        OrderItem orderItem =
            OrderItem.builder()
                .menuItemId(Long.valueOf(menuItemAndQuantity.getKey()))
                .quantity(quantity.intValue())
                .orders(order)
                .price(
                    menuItemPriceDetailsMap.get(menuItemId) != null
                        ? menuItemPriceDetailsMap.get(menuItemId)
                        : BigDecimal.ZERO)
                .build();
        orderItems.add(orderItem);
      }
      return orderItems;
    } catch (Exception e) {
      log.error("Exception while map orderItems with order");
      throw new InternalServerException("Exception while map orderItems with order");
    }
  }

  public String updateOrderStatus(UpdateOrderStatusRequest request) {

    Franchise franchise =
        franchiseRepository
            .findById(request.getFranchiseId())
            .orElseThrow(
                () ->
                    new ResourceNotFoundException(
                        "Franchise not found by id: " + request.getFranchiseId()));
    log.info("Franchise found.");
    Order order = orderRepository.findByFranchiseAndId(franchise, request.getOrderId());
    if (order == null) {
      log.error("Order not found for update");
      throw new ResourceNotFoundException(
          "Order not found by franchiseId: "
              + request.getFranchiseId()
              + " orderId: "
              + request.getOrderId());
    }
    if (order.getStatus() == CustomerOrder.OrderStatusEnum.CANCELLED
        || order.getStatus() == CustomerOrder.OrderStatusEnum.FAILED
        || order.getStatus() == CustomerOrder.OrderStatusEnum.COMPLETED) {
      log.error("Order is not eligible to update.");
      throw new InvalidRequestException("Order is not eligible for update.");
    }
    order.setStatus(CustomerOrder.OrderStatusEnum.fromValue(request.getOrderStatus().getValue()));
    orderRepository.save(order);
    log.info("Order status is updated.");
    return "Order status is updated to " + order.getStatus();
  }
}
