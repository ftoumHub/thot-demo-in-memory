package com.example.demo.commands;

import fr.maif.eventsourcing.SimpleCommand;
import fr.maif.eventsourcing.Type;
import io.vavr.API;
import io.vavr.Lazy;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.math.BigDecimal;

public interface AccountCommand extends SimpleCommand {

    Type<OpenAccount> OpenAccountV1 = Type.create(OpenAccount.class, 1L);
    Type<Deposit> DepositV1 = Type.create(Deposit.class, 1L);
    Type<Withdraw> WithdrawV1 = Type.create(Withdraw.class, 1L);
    Type<CloseAccount> CloseAccountV1 = Type.create(CloseAccount.class, 1L);

    static API.Match.Pattern0<OpenAccount>  $OpenAccount() {
        return API.Match.Pattern0.of(OpenAccount.class);
    }
    static API.Match.Pattern0<Deposit>      $Deposit() {
        return API.Match.Pattern0.of(Deposit.class);
    }
    static API.Match.Pattern0<Withdraw>     $Withdraw() {
        return API.Match.Pattern0.of(Withdraw.class);
    }
    static API.Match.Pattern0<CloseAccount> $CloseAccount() {
        return API.Match.Pattern0.of(CloseAccount.class);
    }


    @RequiredArgsConstructor
    @AllArgsConstructor
    @ToString
    class OpenAccount implements AccountCommand {
        public Lazy<String> id;
        public BigDecimal initialBalance;

        public Lazy<String> entityId() {
            return id;
        }

        public Boolean hasId() {
            return false;
        }
    }

    @AllArgsConstructor
    @ToString
    class Deposit implements AccountCommand {
        public String account;
        public BigDecimal amount;

        public Lazy<String> entityId() {
            return Lazy.of(() -> account);
        }
    }

    @AllArgsConstructor
    @ToString
    class Withdraw implements AccountCommand {
        public String account;
        public BigDecimal amount;

        public Lazy<String> entityId() {
            return Lazy.of(() -> account);
        }
    }

    @AllArgsConstructor
    @ToString
    class CloseAccount implements AccountCommand {
        public String id;

        public Lazy<String> entityId() {
            return Lazy.of(() -> id);
        }
    }
}
