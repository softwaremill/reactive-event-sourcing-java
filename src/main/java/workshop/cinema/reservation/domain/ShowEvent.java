package workshop.cinema.reservation.domain;

import java.io.Serializable;
import java.time.Instant;

public sealed interface ShowEvent extends Serializable {
    ShowId showId();

    Instant createdAt();

    record SeatReserved(ShowId showId, Instant createdAt, SeatNumber seatNumber) implements ShowEvent {
    }
}
