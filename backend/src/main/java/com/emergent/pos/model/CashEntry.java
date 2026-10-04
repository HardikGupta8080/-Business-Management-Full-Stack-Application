package com.emergent.pos.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "cash_entries")
@Getter
@Setter
@NoArgsConstructor
public class CashEntry {

    @Id
    @Column(length = 50)
    private String id;

    private String date;

    // collection, payment
    private String type;

    private Double amount;
    private String description;
    private String reference;
}
