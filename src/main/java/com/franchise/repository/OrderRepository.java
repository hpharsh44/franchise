package com.franchise.repository;

import com.franchise.entity.Franchise;
import com.franchise.entity.Order;
import com.franchise.model.CustomerOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    Order findByCustomerIdAndId(Long customerId, Long orderId);

    Order findByFranchiseAndId(Franchise franchise, Long orderId);

    List<Order> findAllByCustomerId(Long customerId);

////    List<Order> findAllByStatusInOrderByUpdatedAtDesc(List<String> statuses);
//    @Query(nativeQuery = true , value = "select * from orders o where o.status in (:statuses) ")
    List<Order> findAllByStatusInOrderByUpdatedAtDesc(List<CustomerOrder.OrderStatusEnum> statuses);
}
