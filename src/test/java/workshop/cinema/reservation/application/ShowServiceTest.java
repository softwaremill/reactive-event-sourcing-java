package workshop.cinema.reservation.application;

import akka.actor.ActorSystem;
import akka.actor.typed.javadsl.Adapter;
import akka.cluster.sharding.typed.javadsl.ClusterSharding;
import akka.persistence.testkit.PersistenceTestKitPlugin;
import akka.testkit.javadsl.TestKit;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import workshop.cinema.base.domain.Clock;
import workshop.cinema.reservation.domain.SeatNumber;
import workshop.cinema.reservation.domain.ShowId;

import java.util.concurrent.ExecutionException;

import static org.assertj.core.api.Assertions.assertThat;
import static workshop.cinema.reservation.application.Blocking.await;

class ShowServiceTest {

    private static Config config = PersistenceTestKitPlugin.config().withFallback(ConfigFactory.load());
    private static ActorSystem system = ActorSystem.create("es-workshop", config);
    private ClusterSharding sharding = ClusterSharding.get(Adapter.toTyped(system));
    private Clock clock = new Clock.UtcClock();
    private ShowService showService = new ShowService(sharding, clock);

    @AfterAll
    public static void cleanUp() {
        TestKit.shutdownActorSystem(system);
    }

    @Test
    public void shouldCreateShow() throws ExecutionException, InterruptedException {
        //given
        var showId = ShowId.of();

        //when
        var result = await(showService.createShow(showId, "title", 10));

        //then
        assertThat(result).isInstanceOf(ShowEntityResponse.CommandProcessed.class);
    }

    @Test
    public void shouldReserveSeat() throws ExecutionException, InterruptedException {
        //given
        var showId = ShowId.of();
        await(showService.createShow(showId, "title", 10));
        var seatNumber = SeatNumber.of(8);

        //when
        var result = await(showService.reserveSeat(showId, seatNumber));

        //then
        assertThat(result).isInstanceOf(ShowEntityResponse.CommandProcessed.class);
    }

    @Test
    public void shouldCancelReservation() throws ExecutionException, InterruptedException {
        //given
        var showId = ShowId.of();
        await(showService.createShow(showId, "title", 10));
        var seatNumber = SeatNumber.of(8);

        //when
        var reservationResult = await(showService.reserveSeat(showId, seatNumber));

        //then
        assertThat(reservationResult).isInstanceOf(ShowEntityResponse.CommandProcessed.class);

        //when
        var cancellationResult = await(showService.cancelReservation(showId, seatNumber));

        //then
        assertThat(cancellationResult).isInstanceOf(ShowEntityResponse.CommandProcessed.class);
    }

    @Test
    public void shouldFindShowById() throws ExecutionException, InterruptedException {
        //given
        var showId = ShowId.of();
        await(showService.createShow(showId, "title", 10));

        //when
        var show = await(showService.findShowBy(showId)).get();

        //then
        assertThat(show.id()).isEqualTo(showId);
    }

    @Test
    public void shouldReturnEmptyShow() throws ExecutionException, InterruptedException {
        //given
        var showId = ShowId.of();

        //when
        var show = await(showService.findShowBy(showId));

        //then
        assertThat(show.isDefined()).isFalse();
    }
}