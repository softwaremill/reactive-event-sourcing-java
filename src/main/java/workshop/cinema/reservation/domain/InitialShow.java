package workshop.cinema.reservation.domain;

import io.vavr.collection.Map;

import java.io.Serializable;

public record InitialShow(ShowId id, String title, Map<SeatNumber, Seat> seats) implements Serializable {
}
