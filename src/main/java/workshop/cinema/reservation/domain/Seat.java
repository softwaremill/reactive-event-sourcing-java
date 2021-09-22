package workshop.cinema.reservation.domain;

import java.io.Serializable;
import java.math.BigDecimal;

public record Seat(SeatNumber number, SeatStatus status, BigDecimal price) implements Serializable {
    public boolean isAvailable() {
        return status == SeatStatus.AVAILABLE;
    }

    public boolean isReserved() {
        return status == SeatStatus.RESERVED;
    }

    public Seat reserved() {
        return new Seat(number, SeatStatus.RESERVED, price);
    }

    public Seat available() {
        return new Seat(number, SeatStatus.AVAILABLE, price);
    }
}
