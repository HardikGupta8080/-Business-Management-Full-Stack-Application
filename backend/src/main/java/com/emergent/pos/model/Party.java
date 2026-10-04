package com.emergent.pos.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "parties")
@Getter
@Setter
@NoArgsConstructor
public class Party {

    @Id
    @Column(length = 50)
    private String id;

    private String name;
    private String phone;
    private String email;

    @Column(columnDefinition = "TEXT")
    private String address;

    private String city;
    private String state;
    private String pincode;
    private String gstin;

    @Column(name = "`creditLimit`")
    private Double creditLimit;

    @Column(name = "`outstandingBalance`")
    private Double outstandingBalance;

    @Column(name = "`balanceType`")
    private String balanceType;

    @Column(name = "`isActive`")
    private Boolean isActive;

    // customer, supplier, cash
    private String type;
}
