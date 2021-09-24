package workshop.cinema.reservation.application;

import akka.actor.testkit.typed.javadsl.ActorTestKit;
import akka.actor.typed.ActorRef;
import akka.persistence.testkit.javadsl.EventSourcedBehaviorTestKit;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import workshop.cinema.base.domain.Clock;
import workshop.cinema.reservation.application.ShowEntityCommand.ShowCommandEnvelope;
import workshop.cinema.reservation.application.ShowEntityResponse.CommandProcessed;
import workshop.cinema.reservation.application.ShowEntityResponse.CommandRejected;
import workshop.cinema.reservation.domain.FixedClock;
import workshop.cinema.reservation.domain.Show;
import workshop.cinema.reservation.domain.ShowCommand;
import workshop.cinema.reservation.domain.ShowCommand.CancelSeatReservation;
import workshop.cinema.reservation.domain.ShowEvent;
import workshop.cinema.reservation.domain.ShowEvent.SeatReservationCancelled;
import workshop.cinema.reservation.domain.ShowEvent.SeatReserved;
import workshop.cinema.reservation.domain.ShowId;

import static org.assertj.core.api.Assertions.assertThat;
import static workshop.cinema.reservation.domain.ShowCommandError.SEAT_NOT_AVAILABLE;
import static workshop.cinema.reservation.domain.ShowCommandGenerators.randomReserveSeat;

class ShowEntityTest {

    public static final Config UNIT_TEST_AKKA_CONFIGURATION = ConfigFactory.parseString("""
                akka.actor.enable-additional-serialization-bindings = on
                akka.actor.allow-java-serialization = on
                akka.actor.warn-about-java-serializer-usage = off
                akka.loglevel = INFO
            """);

    private static final ActorTestKit testKit =
            ActorTestKit.create(EventSourcedBehaviorTestKit.config().withFallback(UNIT_TEST_AKKA_CONFIGURATION));

    @AfterAll
    public static void cleanUp() {
        testKit.shutdownTestKit();
    }
    
    private Clock clock = new FixedClock();

    @Test
    public void shouldReserveSeat() {
        //given
        var showId = ShowId.of();
        EventSourcedBehaviorTestKit<ShowEntityCommand, ShowEvent, Show> showEntityKit = EventSourcedBehaviorTestKit.create(testKit.system(), ShowEntity.create(showId, clock));
        var reserveSeat = randomReserveSeat(showId);

        //when
        var result = showEntityKit.<ShowEntityResponse>runCommand(replyTo -> toEnvelope(reserveSeat, replyTo));

        //then
        assertThat(result.reply()).isInstanceOf(CommandProcessed.class);
        assertThat(result.event()).isInstanceOf(SeatReserved.class);
        var reservedSeat = result.state().seats().get(reserveSeat.seatNumber()).get();
        assertThat(reservedSeat.isReserved()).isTrue();
    }

    @Test
    public void shouldNotReserveTheAlreadyReservedSeat() {
        //given
        var showId = ShowId.of();
        EventSourcedBehaviorTestKit<ShowEntityCommand, ShowEvent, Show> showEntityKit = EventSourcedBehaviorTestKit.create(testKit.system(), ShowEntity.create(showId, clock));
        var reserveSeat = randomReserveSeat(showId);

        //when
        showEntityKit.<ShowEntityResponse>runCommand(replyTo -> toEnvelope(reserveSeat, replyTo));
        var result = showEntityKit.<ShowEntityResponse>runCommand(replyTo -> toEnvelope(reserveSeat, replyTo));

        //then
        assertThat(result.reply()).isEqualTo(new CommandRejected(SEAT_NOT_AVAILABLE));
        assertThat(result.hasNoEvents()).isTrue();
    }

    @Test
    public void shouldCancelReservation() {
        //given
        var showId = ShowId.of();
        EventSourcedBehaviorTestKit<ShowEntityCommand, ShowEvent, Show> showEntityKit = EventSourcedBehaviorTestKit.create(testKit.system(), ShowEntity.create(showId, clock));
        var reserveSeat = randomReserveSeat(showId);
        var cancelSeatReservation = new CancelSeatReservation(showId, reserveSeat.seatNumber());

        //when
        showEntityKit.<ShowEntityResponse>runCommand(replyTo -> toEnvelope(reserveSeat, replyTo));
        var result = showEntityKit.<ShowEntityResponse>runCommand(replyTo -> toEnvelope(cancelSeatReservation, replyTo));

        //then
        assertThat(result.reply()).isInstanceOf(CommandProcessed.class);
        assertThat(result.event()).isInstanceOf(SeatReservationCancelled.class);
        var seat = result.state().seats().get(reserveSeat.seatNumber()).get();
        assertThat(seat.isReserved()).isFalse();
    }

    @Test
    public void shouldReserveSeat_WithProbe() {
        //given
        var showId = ShowId.of();
        var showEntityRef = testKit.spawn(ShowEntity.create(showId, clock));
        var commandResponseProbe = testKit.<ShowEntityResponse>createTestProbe();
        var showResponseProbe = testKit.<Show>createTestProbe();

        var reserveSeat = randomReserveSeat(showId);

        //when
        showEntityRef.tell(toEnvelope(reserveSeat, commandResponseProbe.ref()));

        //then
        commandResponseProbe.expectMessageClass(CommandProcessed.class);

        //when
        showEntityRef.tell(new ShowEntityCommand.GetShow(showResponseProbe.ref()));

        //then
        Show returnedShow = showResponseProbe.receiveMessage();
        assertThat(returnedShow.seats().get(reserveSeat.seatNumber()).get().isReserved()).isTrue();
    }

    private ShowCommandEnvelope toEnvelope(ShowCommand command, ActorRef<ShowEntityResponse> replyTo) {
        return new ShowCommandEnvelope(command, replyTo);
    }

}