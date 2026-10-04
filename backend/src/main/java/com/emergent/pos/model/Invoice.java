package com.emergent.pos.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "invoices")
@Getter
@Setter
@NoArgsConstructor
public class Invoice {

    @Id
    @Column(length = 50)
    private String id;

    @Column(name = "`invoiceNumber`")
    private String invoiceNumber;

    private String date;

    @Column(name = "`partyId`")
    private String partyId;

    // JSON array of {itemId, itemName, quantity, price, gstRate}
    @Column(columnDefinition = "TEXT")
    private String items;

    private Double subtotal;

    @Column(name = "`gstAmount`")
    private Double gstAmount;

    @Column(name = "`grandTotal`")
    private Double grandTotal;

    private Double discount;

    @Column(name = "`discountAmount`")
    private Double discountAmount;

    @Column(name = "`finalTotal`")
    private Double finalTotal;

    @Column(name = "`paidAmount`")
    private Double paidAmount;

    @Column(name = "`paymentMode`")
    private String paymentMode;

    @Column(columnDefinition = "TEXT")
    private String notes;

    private String status;
}
