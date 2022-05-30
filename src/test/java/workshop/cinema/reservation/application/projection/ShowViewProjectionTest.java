package workshop.cinema.reservation.application.projection;

import akka.actor.typed.ActorSystem;
import akka.actor.typed.SpawnProtocol;
import akka.actor.typed.javadsl.Adapter;
import akka.cluster.sharding.typed.javadsl.ClusterSharding;
import akka.testkit.javadsl.TestKit;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import workshop.cinema.base.application.SpawningBehavior;
import workshop.cinema.base.domain.Clock;
import workshop.cinema.reservation.ReservationConfiguration;
import workshop.cinema.reservation.application.ShowService;
import workshop.cinema.reservation.domain.SeatNumber;
import workshop.cinema.reservation.domain.ShowId;

import java.util.List;
import java.util.concurrent.ExecutionException;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.assertj.core.api.Assertions.assertThat;
import static workshop.cinema.reservation.application.Blocking.await;

/**
 * Run `docker-compose -f docker-compose-jdbc.yml up` in `development` folder
 */
class ShowViewProjectionTest {

    private static Config config = ConfigFactory.load();
    private static ActorSystem<SpawnProtocol.Command> system = ActorSystem.create(SpawningBehavior.create(), "es-workshop", config);
    private ClusterSharding sharding = ClusterSharding.get(system);
    private Clock clock = new Clock.UtcClock();

    private ReservationConfiguration reservationConfiguration = new ReservationConfiguration(system, sharding, clock, config);
    private ShowService showService = reservationConfiguration.showService();
    private ShowViewRepository showViewRepository = reservationConfiguration.showViewRepository();
    private ProjectionLauncher projectionLauncher = reservationConfiguration.projectionLauncher(showViewRepository);

    @AfterAll
    public static void cleanUp() {
        TestKit.shutdownActorSystem(Adapter.toClassic(system));
    }

    @Test
    public void shouldGetAvailableShowView() throws ExecutionException, InterruptedException {
        //given
        var showId1 = ShowId.of();
        var showId2 = ShowId.of();

        await(showService.createShow(showId1, "Matrix", 1));
        await(showService.reserveSeat(showId1, SeatNumber.of(1))); //no more available seat
        await(showService.createShow(showId2, "Snatch", 20));
        await(showService.reserveSeat(showId2, SeatNumber.of(1)));

        //when
        projectionLauncher.runProjections();

        //then
        Awaitility.await().atMost(10, SECONDS).untilAsserted(() -> {
            List<ShowView> showViews = await(showViewRepository.findAvailable());
            assertThat(showViews).contains(new ShowView(showId2.toString(), 19));
        });
        projectionLauncher.shutdownProjections();
    }
}