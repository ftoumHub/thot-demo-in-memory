package com.example.demo;

import akka.actor.ActorSystem;
import com.example.demo.commands.AccountCommandHandler;
import com.example.demo.entities.Account;
import com.example.demo.events.AccountEvent;
import com.example.demo.events.AccountEventHandler;
import fr.maif.eventsourcing.ProcessingSuccess;
import io.vavr.API;
import io.vavr.Tuple0;

import java.math.BigDecimal;

import static io.vavr.API.printf;
import static io.vavr.API.println;

public class DemoApplication {

	private static BigDecimal HUNDRED_EUROS = BigDecimal.valueOf(100);
	private static BigDecimal FIFTY_EUROS = BigDecimal.valueOf(50);

	public static void main(String[] args) {
		ActorSystem system = ActorSystem.create();
		AccountCommandHandler commandHandler = new AccountCommandHandler();
		AccountEventHandler eventHandler = new AccountEventHandler();

		println("===== Bank Creation =====");

		// On créé la banque...
		Bank bank = new Bank(system, commandHandler, eventHandler);

		// On récupère l'id du compte qu'on va créer
		// Les appels sur la banque doivent être bloquant, sinon les commandes ne seront pas exécutés séquentiellement
		String id = bank.createAccount(HUNDRED_EUROS).get().get().currentState.get().id;
		bank.withdraw(id, FIFTY_EUROS).get();
		bank.deposit(id, BigDecimal.valueOf(2000)).get();

		//final ProcessingSuccess<Account, AccountEvent, Tuple0, Tuple0, Tuple0> withdrawProc2 = bank.withdraw(id, BigDecimal.valueOf(10)).get().get();
		//BigDecimal balance = withdrawProc2.currentState.get().balance;

		//println("Previous State : " + withdrawProc2.previousState); // a renommer en storedState?
		//println("Current State : " + withdrawProc2.currentState);
		//println(withdrawProc2.message);

		//printf("Account %s, has balance : %s", id, balance);

		//println("\nLoad Events : ");
		//bank.loadEvents(id).runForeach(API::println, system);
	}

}
