package workshop.cinema.reservation.domain;

import java.io.Serializable;

public record SeatNumber(int value) implements Serializable {

    public static SeatNumber of(int seatNumber) {
        return new SeatNumber(seatNumber);
    }
}
