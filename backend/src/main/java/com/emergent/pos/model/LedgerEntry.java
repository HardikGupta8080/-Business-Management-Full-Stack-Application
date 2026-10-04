package com.emergent.pos.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "ledger_entries")
@Getter
@Setter
@NoArgsConstructor
public class LedgerEntry {

    @Id
    @Column(length = 50)
    private String id;

    private String date;

    @Column(name = "`partyId`")
    private String partyId;

    @Column(name = "`invoiceNumber`")
    private String invoiceNumber;

    private String description;
    private Double debit;
    private Double credit;

    @Column(name = "`runningBalance`")
    private Double runningBalance;

    @Column(name = "`balanceType`")
    private String balanceType;
}
