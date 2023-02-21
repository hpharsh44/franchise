package com.franchise.entity;


import com.franchise.entity.auditable.Auditable;
import com.franchise.enums.FranchiseStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.persistence.*;
import java.util.HashSet;
import java.util.Set;


@Entity
@Table(name = "franchise")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Franchise extends Auditable {

    private String name;

    @Enumerated(EnumType.STRING)
    private FranchiseStatus status = FranchiseStatus.ACTIVE;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, mappedBy = "franchise")
//    @OrderBy("createdAt DESC")
    private Set<Menu> menus = new HashSet<Menu>();

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, mappedBy = "franchise")
//    @OrderBy("createdAt DESC")
    private Set<Order> orders = new HashSet<Order>();
}
