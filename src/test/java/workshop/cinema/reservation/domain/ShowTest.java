package workshop.cinema.reservation.domain;


import io.vavr.collection.List;
import org.junit.jupiter.api.Test;
import workshop.cinema.base.domain.Clock;
import workshop.cinema.reservation.domain.ShowEvent.SeatReserved;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static workshop.cinema.reservation.domain.DomainGenerators.randomShow;
import static workshop.cinema.reservation.domain.ShowCommandError.SEAT_NOT_AVAILABLE;
import static workshop.cinema.reservation.domain.ShowCommandError.SEAT_NOT_EXISTS;
import static workshop.cinema.reservation.domain.ShowCommandGenerators.randomReserveSeat;

class ShowTest {

    private Clock clock = new FixedClock(Instant.now());

    @Test
    public void shouldReserveTheSeat() {
        //given
        var show = randomShow();
        var reserveSeat = randomReserveSeat(show.id());

        //when
        var events = show.process(reserveSeat, clock).get();

        //then
        assertThat(events).containsOnly(new SeatReserved(show.id(), clock.now(), reserveSeat.seatNumber()));
    }

    @Test
    public void shouldReserveTheSeatWithApplyingEvent() {
        //given
        var show = randomShow();
        var reserveSeat = randomReserveSeat(show.id());

        //when
        var events = show.process(reserveSeat, clock).get();
        var updatedShow = apply(show, events);

        //then
        var reservedSeat = updatedShow.seats().get(reserveSeat.seatNumber()).get();
        assertThat(events).containsOnly(new SeatReserved(show.id(), clock.now(), reserveSeat.seatNumber()));
        assertThat(reservedSeat.isAvailable()).isFalse();
    }

    @Test
    public void shouldNotReserveAlreadyReservedSeat() {
        //given
        var show = randomShow();
        var reserveSeat = randomReserveSeat(show.id());

        //when
        var events = show.process(reserveSeat, clock).get();
        var updatedShow = apply(show, events);

        //then
        assertThat(events).containsOnly(new SeatReserved(show.id(), clock.now(), reserveSeat.seatNumber()));

        //when
        ShowCommandError result = updatedShow.process(reserveSeat, clock).getLeft();

        //then
        assertThat(result).isEqualTo(SEAT_NOT_AVAILABLE);
    }

    @Test
    public void shouldNotReserveNotExistingSeat() {
        //given
        var show = randomShow();
        var reserveSeat = new ShowCommand.ReserveSeat(show.id(), new SeatNumber(SeatsCreator.SEAT_RANGE.last() + 1));

        //when
        ShowCommandError result = show.process(reserveSeat, clock).getLeft();

        //then
        assertThat(result).isEqualTo(SEAT_NOT_EXISTS);
    }

    private Show apply(Show show, List<ShowEvent> events) {
        return events.foldLeft(show, Show::apply);
    }
}