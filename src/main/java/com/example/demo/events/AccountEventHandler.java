package com.example.demo.events;

import com.example.demo.entities.Account;
import fr.maif.eventsourcing.EventHandler;
import fr.maif.eventsourcing.EventProcessor;
import io.vavr.collection.List;
import io.vavr.control.Option;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.example.demo.events.AccountEvent.*;
import static io.vavr.API.*;
import static java.math.BigDecimal.ZERO;

public class AccountEventHandler implements EventHandler<Account, AccountEvent> {

    private static final Logger LOGGER = LoggerFactory.getLogger(AccountEventHandler.class);

    @Override
    public Option<Account> deriveState(Option<Account> state, List<AccountEvent> accountEvents) {
        LOGGER.debug("==> Applying events to state : {}", state);
        return accountEvents.foldLeft(state, this::applyEvent);
    }

    @Override
    public Option<Account> applyEvent(Option<Account> previousState, AccountEvent event) {
        LOGGER.debug("==> Evt::applyEvent");
        return Match(event).of(
                Case($AccountOpened(), AccountEventHandler::handleAccountOpened),
                Case($MoneyDeposited(), deposit -> AccountEventHandler.handleMoneyDeposited(previousState, deposit)),
                Case($MoneyWithdrawn(), withdraw -> AccountEventHandler.handleMoneyWithdrawn(previousState, withdraw)),
                Case($AccountClosed(), AccountEventHandler::handleAccountClosed)
        );
    }

    private static Option<Account> handleAccountOpened(AccountOpened event) {
        final Option<Account> newState = Option(new Account(event.accountId, ZERO));
        LOGGER.debug("==> :::::handleAccountOpened, newState is : {}", newState);
        return newState;
    }

    private static Option<Account> handleMoneyDeposited(Option<Account> previousState, MoneyDeposited event) {
        final Option<Account> newState = previousState.map(state -> {
            state.balance = state.balance.add(event.amount);
            return state;
        });
        LOGGER.debug("==> :::::handleMoneyDeposited, newState is : {}", newState);
        return newState;
    }

    private static Option<Account> handleMoneyWithdrawn(Option<Account> previousState, MoneyWithdrawn withdraw) {
        final Option<Account> newState = previousState.map(account -> {
            account.balance = account.balance.subtract(withdraw.amount);
            return account;
        });
        LOGGER.debug("==> :::::handleMoneyWithdrawn, newState is : {}", newState);
        return newState;
    }

    private static Option<Account> handleAccountClosed(AccountClosed close) {
        LOGGER.debug("==> :::::handleAccountClosed");
        return Option.none();
    }
}
