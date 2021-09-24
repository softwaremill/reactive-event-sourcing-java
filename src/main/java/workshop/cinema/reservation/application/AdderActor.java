package workshop.cinema.reservation.application;

import akka.actor.typed.ActorRef;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;

record Add(int a, int b, ActorRef<Integer> replyTo) {
}

public class AdderActor extends AbstractBehavior<Add> {

    public AdderActor(ActorContext<Add> context) {
        super(context);
    }

    @Override
    public Receive<Add> createReceive() {
        return newReceiveBuilder().onMessage(Add.class, add -> {
            add.replyTo().tell(add.a() + add.b());
            return Behaviors.same();
        }).build();
    }
}
