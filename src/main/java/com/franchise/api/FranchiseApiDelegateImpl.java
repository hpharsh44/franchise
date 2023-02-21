package com.franchise.api;

import com.franchise.model.CustomerOrder;
import com.franchise.model.OrderRequest;
import com.franchise.model.UpdateOrderStatusRequest;
import com.franchise.service.FranchiseOrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class FranchiseApiDelegateImpl implements FranchiseApiDelegate {

    private final FranchiseOrderService franchiseOrderService;

    public ResponseEntity<CustomerOrder> placeOrder(OrderRequest orderRequest) {
        return ResponseEntity.ok(franchiseOrderService.placeOrder(orderRequest));
    }

    public ResponseEntity<String> updateOrderStatus(UpdateOrderStatusRequest updateOrderStatusRequest) {
        return ResponseEntity.ok(franchiseOrderService.updateOrderStatus(updateOrderStatusRequest));
    }
}
