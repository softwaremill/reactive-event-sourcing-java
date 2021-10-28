package workshop.cinema.reservation.domain;

import java.io.Serializable;

public sealed interface ShowCommand extends Serializable {
    ShowId showId();

    record CreateShow(ShowId showId, String title, int maxSeats) implements ShowCommand {
    }

    record ReserveSeat(ShowId showId, SeatNumber seatNumber) implements ShowCommand {
    }

    record CancelSeatReservation(ShowId showId, SeatNumber seatNumber) implements ShowCommand {
    }
}
