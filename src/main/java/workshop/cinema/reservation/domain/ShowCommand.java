package workshop.cinema.reservation.domain;

import java.io.Serializable;

public sealed interface ShowCommand extends Serializable {
    ShowId showId();

    record ReserveSeat(ShowId showId, SeatNumber seatNumber) implements ShowCommand {
    }
}
