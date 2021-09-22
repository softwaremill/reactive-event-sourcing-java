package workshop.cinema.reservation.domain;

import workshop.cinema.reservation.domain.ShowCommand.ReserveSeat;

import static workshop.cinema.reservation.domain.DomainGenerators.randomSeatNumber;

public class ShowCommandGenerators {

    public static ReserveSeat randomReserveSeat(ShowId showId) {
        return new ReserveSeat(showId, randomSeatNumber());
    }

}
