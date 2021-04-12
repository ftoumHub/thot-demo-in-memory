package com.example.demo.events;

import fr.maif.eventsourcing.Event;
import fr.maif.eventsourcing.Type;
import io.vavr.API.Match.Pattern0;
import lombok.ToString;

import java.math.BigDecimal;

public abstract class AccountEvent implements Event {

    public static Type<AccountOpened>  AccountOpenedV1 = Type.create(AccountOpened.class, 1L);
    public static Type<MoneyWithdrawn> MoneyWithdrawnV1 = Type.create(MoneyWithdrawn.class, 1L);
    public static Type<MoneyDeposited> MoneyDepositedV1 = Type.create(MoneyDeposited.class, 1L);
    public static Type<AccountClosed>  AccountClosedV1 = Type.create(AccountClosed.class, 1L);

    static Pattern0<AccountOpened>  $AccountOpened() {
        return Pattern0.of(AccountOpened.class);
    }
    static Pattern0<MoneyWithdrawn> $MoneyWithdrawn() {
        return Pattern0.of(MoneyWithdrawn.class);
    }
    static Pattern0<MoneyDeposited> $MoneyDeposited() {
        return Pattern0.of(MoneyDeposited.class);
    }
    static Pattern0<AccountClosed>  $AccountClosed() {
        return Pattern0.of(AccountClosed.class);
    }

    public final String accountId;

    public AccountEvent(String accountId) {
        this.accountId = accountId;
    }

    public String entityId() {
        return accountId;
    }

    @ToString
    public static class MoneyWithdrawn extends AccountEvent {
        public final BigDecimal amount;
        public MoneyWithdrawn(String account, BigDecimal amount) {
            super(account);
            this.amount = amount;
        }

        public Type<MoneyWithdrawn> type() {
            return MoneyWithdrawnV1;
        }
    }

    @ToString
    public static class AccountOpened extends AccountEvent {
        public AccountOpened(String id) {
            super(id);
        }

        public Type<AccountOpened> type() {
            return AccountOpenedV1;
        }
    }

    @ToString
    public static class MoneyDeposited extends AccountEvent {
        public final BigDecimal amount;

        public MoneyDeposited(String id, BigDecimal amount) {
            super(id);
            this.amount = amount;
        }

        public Type<MoneyDeposited> type() {
            return MoneyDepositedV1;
        }
    }

    @ToString
    public static class AccountClosed extends AccountEvent {

        public AccountClosed(String id) {
            super(id);
        }

        public Type<AccountClosed> type() {
            return AccountClosedV1;
        }
    }
}
