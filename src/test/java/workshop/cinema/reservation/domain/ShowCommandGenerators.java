package workshop.cinema.reservation.domain;

import workshop.cinema.reservation.domain.ShowCommand.CancelSeatReservation;
import workshop.cinema.reservation.domain.ShowCommand.CreateShow;
import workshop.cinema.reservation.domain.ShowCommand.ReserveSeat;

import static workshop.cinema.reservation.domain.DomainGenerators.randomSeatNumber;
import static workshop.cinema.reservation.domain.DomainGenerators.randomTitle;

public class ShowCommandGenerators {

    public static CreateShow randomCreateShow(ShowId showId) {
        return new CreateShow(showId, randomTitle(), ShowBuilder.MAX_SEATS);
    }

    public static ReserveSeat randomReserveSeat(ShowId showId) {
        return new ReserveSeat(showId, randomSeatNumber());
    }

    public static CancelSeatReservation randomCancelSeatReservation(ShowId showId) {
        return new CancelSeatReservation(showId, randomSeatNumber());
    }
}
