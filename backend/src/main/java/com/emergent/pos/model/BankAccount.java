package com.emergent.pos.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "bank_accounts")
@Getter
@Setter
@NoArgsConstructor
public class BankAccount {

    @Id
    @Column(length = 50)
    private String id;

    @Column(name = "`bankName`")
    private String bankName;

    @Column(name = "`accountNumber`")
    private String accountNumber;

    @Column(name = "`accountHolder`")
    private String accountHolder;

    @Column(name = "`ifscCode`")
    private String ifscCode;

    private Double balance;

    @Column(name = "`runningBalance`")
    private Double runningBalance;
}
