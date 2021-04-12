package com.example.demo.commands;

import com.example.demo.entities.Account;
import com.example.demo.events.AccountEvent;
import fr.maif.eventsourcing.Command;
import fr.maif.eventsourcing.CommandHandler;
import fr.maif.eventsourcing.Events;
import io.vavr.Tuple0;
import io.vavr.collection.List;
import io.vavr.concurrent.Future;
import io.vavr.control.Either;
import io.vavr.control.Option;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.function.Function;

import static com.example.demo.commands.AccountCommand.*;
import static io.vavr.API.*;

/**
 * By design, a Command has a method 'hasId' returning 'true' (see {@link Command#hasId()}).
 * The {@link fr.maif.eventsourcing.EventProcessor} will try to reload the aggregate (aka 'previousState')
 * from its aggregateStore based on its id.
 * It will then invoke the handleCommand method passing the aggregate it potentially found or an empty option.
 */
public class AccountCommandHandler implements CommandHandler<String, Account, AccountCommand, AccountEvent, Tuple0, Tuple0> {

    private static final Logger LOGGER = LoggerFactory.getLogger(AccountCommandHandler.class);

    /**
     * Fonction de décision prenant l'état courant et une commande en entrée.
     * Celle-ci va décider si l'opération demandée par la commande peut être appliquée
     * à l'état courant en applicant des règles métiers. Si c'est le cas, elle retournera un
     * ou plusieurs évènements.
     */
    public Future<Either<String, Events<AccountEvent, Tuple0>>> handleCommand(Tuple0 transactionContext, Option<Account> state, AccountCommand cmd) {
        return Future(() -> Match(cmd).option(
                    Case($OpenAccount(), this::handleOpening),
                    Case($Withdraw(), withdraw -> this.handleWithdraw(state, withdraw)),
                    Case($Deposit(), deposit -> this.handleDeposit(state, deposit)),
                    Case($CloseAccount(), close -> this.handleClosing(state, close))
                ).toEither(() -> "Unknown command").flatMap(Function.identity())
        );
    }

    private Either<String, Events<AccountEvent, Tuple0>> handleOpening(AccountCommand.OpenAccount opening) {
        LOGGER.debug("==> Cmd::handleOpening");
        if (opening.initialBalance.compareTo(BigDecimal.ZERO) < 0) {
            return Left("Initial balance can't be negative");
        }

        String newId = opening.id.get();
        List<AccountEvent> events = List(new AccountEvent.AccountOpened(newId));
        if (opening.initialBalance.compareTo(BigDecimal.ZERO) > 0) {
            events = events.append(new AccountEvent.MoneyDeposited(newId, opening.initialBalance));
        }

        return Right(Events.events(events));
    }

    private Either<String, Events<AccountEvent, Tuple0>> handleDeposit(Option<Account> state, AccountCommand.Deposit deposit) {
        LOGGER.debug("==> Cmd::handleDeposit");
        return state.toEither("Account does not exist")
                .map(account -> Events.events(new AccountEvent.MoneyDeposited(deposit.account, deposit.amount)));
    }

    private Either<String, Events<AccountEvent, Tuple0>> handleWithdraw(Option<Account> state, AccountCommand.Withdraw withdraw) {
        LOGGER.debug("==> Cmd::handleWithdraw");
        return state.toEither("Account does not exist")
                .flatMap(previous -> {
                    BigDecimal newBalance = previous.balance.subtract(withdraw.amount);
                    if (newBalance.compareTo(BigDecimal.ZERO) < 0) {
                        return Left("Insufficient balance");
                    }
                    return Right(Events.events(new AccountEvent.MoneyWithdrawn(withdraw.account, withdraw.amount)));
                });
    }

    private Either<String, Events<AccountEvent, Tuple0>> handleClosing(Option<Account> state, AccountCommand.CloseAccount close) {
        LOGGER.debug("==> Cmd::handleClosing");
        return state.toEither("No account opened for this id : " + close.id)
                .map(__ -> Events.events(new AccountEvent.AccountClosed(close.id)));
    }
}