package com.lightbend.training.coffeehouse;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;
import akka.testkit.JavaTestKit;
import org.junit.Test;

public class CoffeeHouseTest extends BaseAkkaTestCase {

    @Test
    public void shouldLogMessageWhenCreated() {
        new JavaTestKit(system) {{
            interceptDebugLogMessage(this, ".*[Oo]pen.*", 1, () -> system.actorOf(CoffeeHouse.props(Integer.MAX_VALUE)));
        }};
    }

    @Test
    public void shouldCreateChildActorCalledBaristaWhenCreated() {
        new JavaTestKit(system) {{
            system.actorOf(CoffeeHouse.props(Integer.MAX_VALUE), "create-barista");
            expectActor(this, "/user/create-barista/waiter");
        }};
    }

    @Test
    public void shouldCreateChildActorCalledWaiterWhenCreated() {
        new JavaTestKit(system) {{
            system.actorOf(CoffeeHouse.props(Integer.MAX_VALUE), "create-waiter");
            expectActor(this, "/user/create-waiter/waiter");
        }};
    }

    @Test
    public void shouldCreateGuestActorsWhenCreateGuestMessageSent() {
        new JavaTestKit(system) {{
            ActorRef coffeeHouse = system.actorOf(CoffeeHouse.props(Integer.MAX_VALUE), "create-guest");
            coffeeHouse.tell(new CoffeeHouse.CreateGuest(new Coffee.Akkaccino(), Integer.MAX_VALUE), ActorRef.noSender());
            expectActor(this, "/user/create-guest/$*");
        }};
    }

    @Test
    public void sendingApproveCoffeeShouldForwardPrepareCoffeeIfCaffeineLimitNotReached() {
        new JavaTestKit(system) {{
            ActorRef coffeeHouse = createActor(CoffeeHouse.class, "prepare-coffee", () -> new CoffeeHouse(Integer.MAX_VALUE) {
                @Override
                protected ActorRef createBarista() {
                    return getRef();
                }
            });
            coffeeHouse.tell(new CoffeeHouse.CreateGuest(new Coffee.Akkaccino(), Integer.MAX_VALUE), ActorRef.noSender());
            ActorRef guest = expectActor(this, "/user/prepare-coffee/$*");
            coffeeHouse.tell(new CoffeeHouse.ApproveCoffee(new Coffee.Akkaccino(), guest), getRef());
            expectMsgEquals(new Barista.PrepareCoffee(new Coffee.Akkaccino(), guest));
        }};
    }

    @Test
    public void sendingApproveCoffeeShouldResultInLoggingStatusMessageWhenLimitReached() {
        new JavaTestKit(system) {{
            ActorRef coffeeHouse = system.actorOf(CoffeeHouse.props(1), "caffeine-limit");
            coffeeHouse.tell(new CoffeeHouse.CreateGuest(new Coffee.Akkaccino(), Integer.MAX_VALUE), ActorRef.noSender());
            ActorRef guest = expectActor(this, "/user/caffeine-limit/$*");
            interceptInfoLogMessage(this, ".*[Ss]orry.*", 1, () -> coffeeHouse.tell(
                    new CoffeeHouse.ApproveCoffee(new Coffee.Akkaccino(), guest), ActorRef.noSender()));
        }};
    }

    @Test
    public void sendingApproveCoffeeShouldResultInStoppingGuestWhenLimitReached() {
        new JavaTestKit(system) {{
            ActorRef coffeeHouse = system.actorOf(CoffeeHouse.props(1), "guest-terminated");
            coffeeHouse.tell(new CoffeeHouse.CreateGuest(new Coffee.Akkaccino(), Integer.MAX_VALUE), ActorRef.noSender());
            ActorRef guest = expectActor(this, "/user/guest-terminated/$*");
            watch(guest);
            coffeeHouse.tell(new CoffeeHouse.ApproveCoffee(new Coffee.Akkaccino(), guest), ActorRef.noSender());
            expectTerminated(guest);
        }};
    }

    @Test
    public void onTerminationOfGuestCoffeeHouseShouldRemoveGuestFromBookkeeper() {
        new JavaTestKit(system) {{
            ActorRef coffeeHouse = system.actorOf(CoffeeHouse.props(1), "guest-removed");
            coffeeHouse.tell(new CoffeeHouse.CreateGuest(new Coffee.Akkaccino(), Integer.MAX_VALUE), ActorRef.noSender());
            ActorRef guest = expectActor(this, "/user/guest-removed/$*");
            interceptDebugLogMessage(this, ".*[Rr]emoved.*", 1, () -> {
                coffeeHouse.tell(new CoffeeHouse.ApproveCoffee(new Coffee.Akkaccino(), guest), ActorRef.noSender());
            });
        }};
    }

    @Test
    public void shouldStopGuestOnFailure() {
        new JavaTestKit(system) {{
            ActorRef coffeeHouse = system.actorOf(CoffeeHouse.props(1), "guest-stopped");
            coffeeHouse.tell(new CoffeeHouse.CreateGuest(new Coffee.Akkaccino(), 0), ActorRef.noSender());
            ActorRef guest = expectActor(this, "/user/guest-stopped/$*");
            watch(guest);
            coffeeHouse.tell(new CoffeeHouse.ApproveCoffee(new Coffee.Akkaccino(), guest), ActorRef.noSender());
            expectTerminated(guest);
        }};
    }

    @Test
    public void shouldRestartWaiterAndResendPrepareCoffeeToBaristaOnFailure() {
        new JavaTestKit(system) {{
            createActor(CoffeeHouse.class, "resend-prepare-coffee", () -> new CoffeeHouse(Integer.MAX_VALUE) {
                @Override
                protected ActorRef createBarista() {
                    return getRef();
                }

                @Override
                protected ActorRef createWaiter() { //stubbing out the waiter actor to always throw exception
                    return context().actorOf(Props.create(AbstractActor.class, () -> new AbstractActor() {
                        @Override
                        public Receive createReceive() {
                            return receiveBuilder().matchAny(o -> {
                                throw new Waiter.FrustratedException(new Coffee.Akkaccino(), system.deadLetters());
                            }).build();
                        }
                    }), "waiter");
                }
            });
            ActorRef waiter = expectActor(this, "/user/resend-prepare-coffee/waiter");
            waiter.tell("Blow up", ActorRef.noSender());
            expectMsgEquals(new Barista.PrepareCoffee(new Coffee.Akkaccino(), system.deadLetters()));
        }};
    }
}