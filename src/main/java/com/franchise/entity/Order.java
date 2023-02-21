package com.franchise.entity;


import com.franchise.entity.auditable.Auditable;
import com.franchise.model.CustomerOrder;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.DynamicUpdate;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "orders")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@DynamicUpdate
public class Order extends Auditable {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "franchise")
    private Franchise franchise;

    private Long menuId;

    private Long customerId;

    private BigDecimal total;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private CustomerOrder.OrderStatusEnum status = CustomerOrder.OrderStatusEnum.FAILED;

    @Enumerated(EnumType.STRING)
    private CustomerOrder.OrderTypeEnum orderType;

    private LocalDateTime pickUpTime;

    @OneToMany(mappedBy = "orders", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
//    @OrderBy("id ASC")
    private Set<OrderItem> orderItems = new HashSet<OrderItem>();
}
