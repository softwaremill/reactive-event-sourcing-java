package workshop.cinema.reservation.api;

import workshop.cinema.reservation.domain.Seat;

import java.math.BigDecimal;

public record SeatResponse(int number, String status, BigDecimal price) {

    public static SeatResponse from(Seat seat) {
        return new SeatResponse(seat.number().value(), seat.status().name(), seat.price());
    }
}
