package workshop.cinema.base.application;

import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.Behaviors;

public class VoidBehavior {

    public static Behavior<Void> create() {
        return Behaviors.receive(Void.class).build();
    }
}
