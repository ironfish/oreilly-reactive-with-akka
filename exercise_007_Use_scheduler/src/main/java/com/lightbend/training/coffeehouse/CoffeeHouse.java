package com.lightbend.training.coffeehouse;

import akka.actor.AbstractLoggingActor;
import akka.actor.ActorRef;
import akka.actor.Props;
import scala.concurrent.duration.Duration;
import scala.concurrent.duration.FiniteDuration;

import static com.google.common.base.Preconditions.checkNotNull;
import static java.util.concurrent.TimeUnit.MILLISECONDS;

public class CoffeeHouse extends AbstractLoggingActor {

    //===========================================================================
    // ANSWER
    //===========================================================================
    // @todo For `finishCoffeeDuration`, use a configuration value with key `coffee-house.guest.finish-coffee-duration`.
    // @todo To get the configuration value, use the `getDuration` method on `context().system().settings().config()`.
    private final FiniteDuration guestFinishCoffeeDuration =
            Duration.create(context().system().settings().config().getDuration(
                    "coffee-house.guest.finish-coffee-duration", MILLISECONDS), MILLISECONDS);

    private final ActorRef waiter =
            context().actorOf(Waiter.props(), "waiter");

    public CoffeeHouse() {
        log().debug("CoffeeHouse Open");
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(CreateGuest.class, createGuest -> createGuest(createGuest.favoriteCoffee))
                .build();
    }

    static Props props() {
        return Props.create(CoffeeHouse.class, CoffeeHouse::new);
    }

    //===========================================================================
    // ANSWER
    //===========================================================================
    private void createGuest(Coffee favoriteCoffee) {
        // todo Adjust the code for creating a new `Guest`.
        context().actorOf(Guest.props(waiter, favoriteCoffee, guestFinishCoffeeDuration));
    }

    public static final class CreateGuest {

        final Coffee favoriteCoffee;

        CreateGuest(final Coffee favoriteCoffee) {
            checkNotNull(favoriteCoffee, "Favorite coffee cannot be null");
            this.favoriteCoffee = favoriteCoffee;
        }

        @Override
        public String toString() {
            return "CreateGuest{favoriteCoffee=" + favoriteCoffee + "}";
        }

        @Override
        public boolean equals(Object o) {
            if (o == this) return true;
            if (o instanceof CreateGuest) {
                CreateGuest that = (CreateGuest) o;
                return this.favoriteCoffee.equals(that.favoriteCoffee);
            }
            return false;
        }

        @Override
        public int hashCode() {
            int h = 1;
            h *= 1000003;
            h ^= favoriteCoffee.hashCode();
            return h;
        }
    }
}
