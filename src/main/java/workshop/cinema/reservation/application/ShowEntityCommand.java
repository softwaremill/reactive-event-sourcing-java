package workshop.cinema.reservation.application;

import akka.actor.typed.ActorRef;
import workshop.cinema.reservation.domain.Show;
import workshop.cinema.reservation.domain.ShowCommand;

import java.io.Serializable;

public sealed interface ShowEntityCommand extends Serializable {

    record ShowCommandEnvelope(ShowCommand command, ActorRef<ShowEntityResponse> replyTo) implements ShowEntityCommand {
    }

    record GetShow(ActorRef<Show> replyTo) implements ShowEntityCommand {
    }
}
