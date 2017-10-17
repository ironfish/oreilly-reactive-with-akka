/*
 * Copyright © 2014 Typesafe, Inc. All rights reserved.
 */

package com.lightbend.training.coffeehouse;

import akka.actor.AbstractLoggingActor;
import akka.actor.ActorRef;
import akka.actor.Props;
import scala.concurrent.duration.FiniteDuration;

public class Guest extends AbstractLoggingActor {

    private final ActorRef waiter;

    private final Coffee favoriteCoffee;

    //===========================================================================
    // ANSWER
    //===========================================================================
    // @todo Add a `finishCoffeeDuration` parameter of type `scala.concurrent.duration.FiniteDuration`.
    // private final FiniteDuration finishCoffeeDuration;

    private int coffeeCount = 0;

    public Guest(ActorRef waiter, Coffee favoriteCoffee) {
        this.waiter = waiter;
        this.favoriteCoffee = favoriteCoffee;
    }

    //===========================================================================
    // ANSWER
    //===========================================================================
    // @todo set local variable finishCoffeeDuration
    // public Guest(ActorRef waiter, Coffee favoriteCoffee, FiniteDuration finishCoffeeDuration) {
    //     this.waiter = waiter;
    //     this.favoriteCoffee = favoriteCoffee;
    //     this.finishCoffeeDuration = finishCoffeeDuration;
    //     orderFavoriteCoffee();
    // }

    @Override
    public Receive createReceive() {
        return receiveBuilder().
                match(Waiter.CoffeeServed.class, coffeeServed -> {
                    coffeeCount++;
                    log().info("Enjoying my {} yummy {}!", coffeeCount, coffeeServed.coffee);
                    //===========================================================================
                    // ANSWER
                    //===========================================================================
                    // @todo Change the behavior on receiving `CoffeeServed` to schedule the sending of `CoffeeFinished` to the `Guest`.
                    // scheduleCoffeeFinished();
                }).
                match(CoffeeFinished.class, coffeeFinished ->
                        orderFavoriteCoffee()
                ).build();
    }

    public static Props props(final ActorRef waiter, final Coffee favoriteCoffee) {
        return Props.create(Guest.class, () -> new Guest(waiter, favoriteCoffee));
    }

    //===========================================================================
    // ANSWER
    //===========================================================================
    // @todo add waiter ActorRef to props
    // @todo add Coffee to props
    // @todo add FiniteDuration to props
    // public static Props props(final ActorRef waiter, final Coffee favoriteCoffee, FiniteDuration finishCoffeeDuration) {
    //     return Props.create(Guest.class, () -> new Guest(waiter, favoriteCoffee, finishCoffeeDuration));
    // }

    private void orderFavoriteCoffee() {
        waiter.tell(new Waiter.ServeCoffee(favoriteCoffee), self());
    }

    //===========================================================================
    // ANSWER
    //===========================================================================
    // @todo implement scheduleCoffeeFinished method
    // private void scheduleCoffeeFinished() {
    //     context().system().scheduler().scheduleOnce(finishCoffeeDuration, self(),
    //             CoffeeFinished.Instance, context().dispatcher(), self());
    // }

    public static final class CoffeeFinished {

        public static final CoffeeFinished Instance =
                new CoffeeFinished();

        private CoffeeFinished() {
        }
    }
}
