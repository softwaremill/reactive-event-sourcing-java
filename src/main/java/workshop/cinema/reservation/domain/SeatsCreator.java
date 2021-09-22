package workshop.cinema.reservation.domain;

import io.vavr.Tuple2;
import io.vavr.collection.HashMap;
import io.vavr.collection.Map;
import io.vavr.collection.Vector;

import java.math.BigDecimal;

public class SeatsCreator {
    static final Vector<Integer> SEAT_RANGE = Vector.rangeClosed(1, 10);

    public static Map<SeatNumber, Seat> createSeats(BigDecimal seatPrice) {
        var seats = SEAT_RANGE.map(seatNum -> {
            var seatNumber = new SeatNumber(seatNum);
            var seat = new Seat(seatNumber, SeatStatus.AVAILABLE, seatPrice);
            return new Tuple2<>(seatNumber, seat);
        });
        return HashMap.ofEntries(seats);
    }
}
