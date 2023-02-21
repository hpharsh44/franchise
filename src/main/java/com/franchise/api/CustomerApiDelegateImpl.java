package com.franchise.api;

import com.franchise.model.CustomerOrder;
import com.franchise.service.CustomerOrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CustomerApiDelegateImpl implements CustomerApiDelegate {

    private final CustomerOrderService customerOrderService;

    public ResponseEntity<List<CustomerOrder>> getAllCustomerOrder(Long customerId) {
        return ResponseEntity.ok(customerOrderService.getAllCustomerOrder(customerId));
    }

    public ResponseEntity<CustomerOrder> getCustomerOrderByOrderId(Long customerId, Long orderId) {
        return ResponseEntity.ok(customerOrderService.getCustomerOrderByOrderId(customerId, orderId));
    }
}
