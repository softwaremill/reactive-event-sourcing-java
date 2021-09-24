package workshop.cinema.reservation.domain;


import io.vavr.collection.List;
import org.junit.jupiter.api.Test;
import workshop.cinema.base.domain.Clock;
import workshop.cinema.reservation.domain.ShowEvent.SeatReservationCancelled;
import workshop.cinema.reservation.domain.ShowEvent.SeatReserved;

import java.math.BigDecimal;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static workshop.cinema.reservation.domain.DomainGenerators.randomShow;
import static workshop.cinema.reservation.domain.ShowBuilder.showBuilder;
import static workshop.cinema.reservation.domain.ShowCommandError.SEAT_NOT_AVAILABLE;
import static workshop.cinema.reservation.domain.ShowCommandError.SEAT_NOT_EXISTS;
import static workshop.cinema.reservation.domain.ShowCommandError.SEAT_NOT_RESERVED;
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

    @Test
    public void shouldCancelSeatReservation() {
        //given
        var reservedSeat = new Seat(new SeatNumber(2), SeatStatus.RESERVED, new BigDecimal("123"));
        var show = showBuilder().withRandomSeats().withSeat(reservedSeat).build();
        var cancelSeatReservation = new ShowCommand.CancelSeatReservation(show.id(), reservedSeat.number());

        //when
        var events = show.process(cancelSeatReservation, clock).get();

        //then
        assertThat(events).containsOnly(new SeatReservationCancelled(show.id(), clock.now(), reservedSeat.number()));
    }

    @Test
    public void shouldNotCancelReservationOfAvailableSeat() {
        //given
        var availableSeat = new Seat(new SeatNumber(2), SeatStatus.AVAILABLE, new BigDecimal("123"));
        var show = showBuilder().withRandomSeats().withSeat(availableSeat).build();
        var cancelSeatReservation = new ShowCommand.CancelSeatReservation(show.id(), availableSeat.number());

        //when
        var result = show.process(cancelSeatReservation, clock).getLeft();

        //then
        assertThat(result).isEqualTo(SEAT_NOT_RESERVED);
    }

    private Show apply(Show show, List<ShowEvent> events) {
        return events.foldLeft(show, Show::apply);
    }
}