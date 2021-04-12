package com.example.demo;

import akka.NotUsed;
import akka.actor.ActorSystem;
import akka.stream.javadsl.Source;
import com.example.demo.commands.AccountCommand;
import com.example.demo.commands.AccountCommand.CloseAccount;
import com.example.demo.commands.AccountCommand.Deposit;
import com.example.demo.commands.AccountCommand.OpenAccount;
import com.example.demo.commands.AccountCommand.Withdraw;
import com.example.demo.commands.AccountCommandHandler;
import com.example.demo.entities.Account;
import com.example.demo.events.AccountEvent;
import com.example.demo.events.AccountEventHandler;
import com.fasterxml.uuid.Generators;
import com.fasterxml.uuid.impl.TimeBasedGenerator;
import fr.maif.eventsourcing.*;
import fr.maif.eventsourcing.impl.InMemoryEventStore;
import io.vavr.Lazy;
import io.vavr.Tuple;
import io.vavr.Tuple0;
import io.vavr.concurrent.Future;
import io.vavr.control.Either;
import io.vavr.control.Option;

import java.math.BigDecimal;
import java.util.function.Function;

import static io.vavr.API.printf;
import static io.vavr.API.println;
import static io.vavr.collection.List.empty;

public class Bank {

    private static final TimeBasedGenerator UUIDgenerator = Generators.timeBasedGenerator();

    private final EventProcessor<String, Account, AccountCommand, AccountEvent, Tuple0, Tuple0, Tuple0, Tuple0> eventProcessor;

    private EventStore<Tuple0, AccountEvent, Tuple0, Tuple0> eventStore;

    public Bank(ActorSystem actorSystem, AccountCommandHandler cmdHandler, AccountEventHandler evtHandler) {
        this.eventStore = InMemoryEventStore.create(actorSystem);
        this.eventProcessor = new EventProcessor<>(
                actorSystem,
                this.eventStore,
                noOpTransactionManager(),
                cmdHandler,
                evtHandler,
                empty()
        );
    }

    private TransactionManager<Tuple0> noOpTransactionManager() {
        return new TransactionManager<Tuple0>() {
            @Override
            public <T> Future<T> withTransaction(Function<Tuple0, Future<T>> function) {
                return function.apply(Tuple.empty());
            }
        };
    }

    public Future<Either<String, ProcessingSuccess<Account, AccountEvent, Tuple0, Tuple0, Tuple0>>> createAccount(BigDecimal amount) {
        Lazy<String> lazyId = Lazy.of(() -> UUIDgenerator.generate().toString());
        printf("==> Bank::createAccount with id %s\n", lazyId.get());
        return eventProcessor.processCommand(new OpenAccount(lazyId, amount));
    }

    public Future<Either<String, ProcessingSuccess<Account, AccountEvent, Tuple0, Tuple0, Tuple0>>> withdraw(String account, BigDecimal amount) {
        printf("==> Bank::withdraw %s\n", amount);
        return eventProcessor.processCommand(new Withdraw(account, amount));
    }

    public Future<Either<String, ProcessingSuccess<Account, AccountEvent, Tuple0, Tuple0, Tuple0>>> deposit(String account, BigDecimal amount) {
        printf("==> Bank::deposit %s\n", amount);
        return eventProcessor.processCommand(new Deposit(account, amount));
    }

    public Future<Either<String, ProcessingSuccess<Account, AccountEvent, Tuple0, Tuple0, Tuple0>>> close(String account) {
        println("==> Bank::close");
        return eventProcessor.processCommand(new CloseAccount(account));
    }

    public Future<Option<Account>> findAccountById(String id) {
        println("==> Bank::findAccountById : " + id);
        return eventProcessor.getAggregate(id);
    }

    public Source<EventEnvelope<AccountEvent, Tuple0, Tuple0>, NotUsed> loadEvents(String id) {
        println("==> Bank::loadEvents : " + id);
        return eventProcessor.eventStore().loadEvents(id);
    }
}
