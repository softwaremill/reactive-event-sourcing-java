package workshop.cinema.reservation.application;

import workshop.cinema.reservation.domain.ShowCommandError;

import java.io.Serializable;

public sealed interface ShowEntityResponse extends Serializable {

    final class CommandProcessed implements ShowEntityResponse {
    }

    record CommandRejected(ShowCommandError error) implements ShowEntityResponse {
    }
}
