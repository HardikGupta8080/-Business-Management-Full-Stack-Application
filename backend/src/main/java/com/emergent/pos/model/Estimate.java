package com.emergent.pos.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "estimates")
@Getter
@Setter
@NoArgsConstructor
public class Estimate {

    @Id
    @Column(length = 50)
    private String id;

    @Column(name = "`estimateNumber`")
    private String estimateNumber;

    private String date;

    @Column(name = "`partyId`")
    private String partyId;

    // JSON array
    @Column(columnDefinition = "TEXT")
    private String items;

    private Double subtotal;

    @Column(name = "`gstAmount`")
    private Double gstAmount;

    @Column(name = "`grandTotal`")
    private Double grandTotal;

    private Double discount;

    @Column(columnDefinition = "TEXT")
    private String notes;
}
