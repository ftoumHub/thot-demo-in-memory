package com.example.demo.entities;

import fr.maif.eventsourcing.AbstractState;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.math.BigDecimal;

@ToString
@NoArgsConstructor
public class Account extends AbstractState<Account> {

    public String id;
    public BigDecimal balance;
    public long sequenceNum;

    public Account(String id, BigDecimal balance) {
        this.id = id;
        this.balance = balance;
    }

    public Long sequenceNum() {
        return sequenceNum;
    }

    public Account withSequenceNum(Long sequenceNum) {
        this.sequenceNum = sequenceNum;
        return this;
    }
}
