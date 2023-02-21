package com.franchise.api;

import com.franchise.model.CustomerOrder;
import com.franchise.service.CustomerOrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class CustomerApiDelegateImpl implements CustomerApiDelegate {

    private final CustomerOrderService customerOrderService;

    public ResponseEntity<List<CustomerOrder>> getAllCustomerOrder(Long customerId) {
      log.info("get all request received for customerId: ",customerId);
        List<CustomerOrder> response = customerOrderService.getAllCustomerOrder(customerId);
        log.info("get all request completed for customerId: ",customerId);
        return ResponseEntity.ok(response);
    }

    public ResponseEntity<CustomerOrder> getCustomerOrderByOrderId(Long customerId, Long orderId) {
        log.info("get order by  customerId and Order Id ",customerId,orderId);
        CustomerOrder response = customerOrderService.getCustomerOrderByOrderId(customerId, orderId);
        log.info("get order by  customerId and orderId completed",customerId,orderId);
        return ResponseEntity.ok(response);
    }
}
