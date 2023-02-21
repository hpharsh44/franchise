package com.franchise.api;

import com.franchise.model.CustomerOrder;
import com.franchise.model.OrderRequest;
import com.franchise.model.UpdateOrderStatusRequest;
import com.franchise.service.FranchiseOrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class FranchiseApiDelegateImpl implements FranchiseApiDelegate {

    private final FranchiseOrderService franchiseOrderService;

    public ResponseEntity<CustomerOrder> placeOrder(OrderRequest orderRequest) {
        log.info("PlaceOrder request received.");
        CustomerOrder response = franchiseOrderService.placeOrder(orderRequest);
        log.info("PlaceOrder request completed.");
        return ResponseEntity.ok(response);
    }

    public ResponseEntity<String> updateOrderStatus(UpdateOrderStatusRequest updateOrderStatusRequest) {
        log.info("update order request received.");
        String response = franchiseOrderService.updateOrderStatus(updateOrderStatusRequest);
        log.info("update order request received.");
        return ResponseEntity.ok(response);
    }
}
