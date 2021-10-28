package workshop.cinema.reservation.domain;

import io.vavr.Tuple2;
import io.vavr.collection.HashMap;
import io.vavr.collection.Map;
import io.vavr.collection.Vector;
import io.vavr.control.Either;
import workshop.cinema.base.domain.Clock;
import workshop.cinema.reservation.domain.ShowCommand.CreateShow;
import workshop.cinema.reservation.domain.ShowEvent.ShowCreated;

import java.math.BigDecimal;

import static io.vavr.control.Either.left;
import static io.vavr.control.Either.right;
import static workshop.cinema.reservation.domain.ShowCommandError.TOO_MANY_SEATS;

public class ShowCreator {

    public static final BigDecimal INITIAL_PRICE = new BigDecimal("100");

    public static Either<ShowCommandError, ShowCreated> create(CreateShow createShow, Clock clock) {
        //more domain validation here
        if (createShow.maxSeats() > 100) {
            return left(TOO_MANY_SEATS);
        } else {
            var initialShow = new InitialShow(createShow.showId(), createShow.title(), createSeats(INITIAL_PRICE, createShow.maxSeats()));
            var showCreated = new ShowCreated(createShow.showId(), clock.now(), initialShow);
            return right(showCreated);
        }
    }

    public static Map<SeatNumber, Seat> createSeats(BigDecimal seatPrice, int maxSeats) {
        var seatRange = Vector.range(0, maxSeats);
        var seats = seatRange.map(seatNum -> {
            var seatNumber = new SeatNumber(seatNum);
            var seat = new Seat(seatNumber, SeatStatus.AVAILABLE, seatPrice);
            return new Tuple2<>(seatNumber, seat);
        });
        return HashMap.ofEntries(seats);
    }
}
