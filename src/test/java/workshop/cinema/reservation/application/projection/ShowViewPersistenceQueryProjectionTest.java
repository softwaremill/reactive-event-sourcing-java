package workshop.cinema.reservation.application.projection;

import akka.Done;
import akka.actor.ActorSystem;
import akka.actor.typed.javadsl.Adapter;
import akka.cluster.sharding.typed.javadsl.ClusterSharding;
import akka.persistence.query.EventEnvelope;
import akka.persistence.query.Offset;
import akka.persistence.query.PersistenceQuery;
import akka.persistence.testkit.PersistenceTestKitPlugin;
import akka.persistence.testkit.query.javadsl.PersistenceTestKitReadJournal;
import akka.testkit.javadsl.TestKit;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import workshop.cinema.base.domain.Clock;
import workshop.cinema.reservation.application.ShowEntity;
import workshop.cinema.reservation.application.ShowService;
import workshop.cinema.reservation.domain.SeatNumber;
import workshop.cinema.reservation.domain.ShowEvent;
import workshop.cinema.reservation.domain.ShowId;
import workshop.cinema.reservation.infrastructure.InMemoryShowViewRepository;

import java.util.List;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutionException;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.assertj.core.api.Assertions.assertThat;
import static workshop.cinema.reservation.application.Blocking.await;
import static workshop.cinema.reservation.application.ShowEntity.persistenceId;

class ShowViewPersistenceQueryProjectionTest {

    private static Config config = PersistenceTestKitPlugin.config().withFallback(ConfigFactory.load());
    private static ActorSystem system = ActorSystem.create("es-workshop", config);
    private ClusterSharding sharding = ClusterSharding.get(Adapter.toTyped(system));
    private Clock clock = new Clock.UtcClock();
    private ShowService showService = new ShowService(sharding, clock);
    private ShowViewRepository showViewRepository = new InMemoryShowViewRepository();
    private PersistenceTestKitReadJournal readJournal = PersistenceQuery.get(system)
            .getReadJournalFor(PersistenceTestKitReadJournal.class, PersistenceTestKitReadJournal.Identifier());


    @AfterAll
    public static void cleanUp() {
        TestKit.shutdownActorSystem(system);
    }

    @Test
    public void shouldGetAvailableShowViewsUsingByPersistenceId() throws ExecutionException, InterruptedException {
        //given
        var showId1 = ShowId.of();
        var showId2 = ShowId.of();

        await(showService.createShow(showId1, "Matrix", 1));
        await(showService.reserveSeat(showId1, SeatNumber.of(1))); //no more available seat
        await(showService.createShow(showId2, "Snatch", 20));
        await(showService.reserveSeat(showId2, SeatNumber.of(1)));

        //when
        readJournal.eventsByPersistenceId(persistenceId(showId1).id(), 0, Long.MAX_VALUE)
                .mapAsync(1, this::processEvent)
                .run(system);

        readJournal.eventsByPersistenceId(persistenceId(showId2).id(), 0, Long.MAX_VALUE)
                .mapAsync(1, this::processEvent)
                .run(system);

        //then
        Awaitility.await().atMost(10, SECONDS).untilAsserted(() -> {
            List<ShowView> showViews = await(showViewRepository.findAvailable());
            assertThat(showViews).contains(new ShowView(showId2.toString(), 19));
        });
    }

    @Test
    public void shouldGetAvailableShowViewsUsingByTag() throws ExecutionException, InterruptedException {
        //given
        var showId1 = ShowId.of();
        var showId2 = ShowId.of();

        await(showService.createShow(showId1, "Matrix", 1));
        await(showService.reserveSeat(showId1, SeatNumber.of(1))); //no more available seat
        await(showService.createShow(showId2, "Snatch", 20));
        await(showService.reserveSeat(showId2, SeatNumber.of(1)));

        //when
        readJournal.currentEventsByTag(ShowEntity.SHOW_EVENT_TAG, Offset.noOffset())
                .mapAsync(1, this::processEvent)
                .run(system);

        //then
        Awaitility.await().atMost(10, SECONDS).untilAsserted(() -> {
            List<ShowView> showViews = await(showViewRepository.findAvailable());
            assertThat(showViews).contains(new ShowView(showId2.toString(), 19));
        });
    }

    private CompletionStage<Done> processEvent(EventEnvelope eventEnvelope) {
        if (eventEnvelope.event() instanceof ShowEvent showEvent) {
            return switch (showEvent) {
                case ShowEvent.ShowCreated showCreated -> showViewRepository.save(showCreated.showId(), showCreated.initialShow().seats().size());
                case ShowEvent.SeatReserved seatReserved -> showViewRepository.decrementAvailability(seatReserved.showId());
                case ShowEvent.SeatReservationCancelled seatReservationCancelled -> showViewRepository.incrementAvailability(seatReservationCancelled.showId());
            };
        } else {
            throw new IllegalStateException("Unrecognized event type");
        }
    }

}