package com.emergent.pos.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "purchase_invoices")
@Getter
@Setter
@NoArgsConstructor
public class PurchaseInvoice {

    @Id
    @Column(length = 50)
    private String id;

    @Column(name = "`invoiceNumber`")
    private String invoiceNumber;

    private String date;

    @Column(name = "`supplierId`")
    private String supplierId;

    // JSON array
    @Column(columnDefinition = "TEXT")
    private String items;

    @Column(name = "`totalAmount`")
    private Double totalAmount;

    @Column(name = "`gstAmount`")
    private Double gstAmount;

    @Column(name = "`grandTotal`")
    private Double grandTotal;

    @Column(name = "`paymentStatus`")
    private String paymentStatus;
}
