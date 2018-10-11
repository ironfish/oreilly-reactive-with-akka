// //===========================================================================
// // ANSWER
// //===========================================================================
//
// package com.lightbend.training.coffeehouse;
//
// import akka.actor.AbstractLoggingActor;
// import akka.actor.Props;
//
// // @todo create guest class
// public class Guest extends AbstractLoggingActor {
//
//     public Guest() {
//     }
//
//     // @todo Implement the behavior as `empty`
//     @Override
//     public Receive createReceive() {
//         return receiveBuilder().matchAny(this::unhandled).build();
//     }
//
//     // @todo Create a `Props` factory for `Guest`
//     public static Props props() {
//         return Props.create(Guest.class, Guest::new);
//     }
// }
