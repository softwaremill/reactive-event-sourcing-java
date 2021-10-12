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
import workshop.cinema.reservation.domain.ShowId;

import java.util.concurrent.ExecutionException;

import static org.assertj.core.api.Assertions.assertThat;
import static workshop.cinema.reservation.domain.DomainGenerators.randomSeatNumber;

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
    public void shouldReserveSeat() throws ExecutionException, InterruptedException {
        //given
        var showId = ShowId.of();
        var seatNumber = randomSeatNumber();

        //when
        var result = showService.reserveSeat(showId, seatNumber).toCompletableFuture().get();

        //then
        assertThat(result).isInstanceOf(ShowEntityResponse.CommandProcessed.class);
    }

    @Test
    public void shouldCancelReservation() throws ExecutionException, InterruptedException {
        //given
        var showId = ShowId.of();
        var seatNumber = randomSeatNumber();

        //when
        var reservationResult = showService.reserveSeat(showId, seatNumber).toCompletableFuture().get();

        //then
        assertThat(reservationResult).isInstanceOf(ShowEntityResponse.CommandProcessed.class);

        //when
        var cancellationResult = showService.cancelReservation(showId, seatNumber).toCompletableFuture().get();

        //then
        assertThat(cancellationResult).isInstanceOf(ShowEntityResponse.CommandProcessed.class);
    }

    @Test
    public void shouldFindShowById() throws ExecutionException, InterruptedException {
        //given
        var showId = ShowId.of();

        //when
        var show = showService.findShowBy(showId).toCompletableFuture().get();

        //then
        assertThat(show.id()).isEqualTo(showId);
    }
}