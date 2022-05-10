package workshop.cinema.reservation.application.projection;

import akka.Done;
import akka.actor.typed.ActorSystem;
import akka.persistence.query.EventEnvelope;
import akka.persistence.query.javadsl.EventsByPersistenceIdQuery;
import workshop.cinema.reservation.domain.ShowEvent;
import workshop.cinema.reservation.domain.ShowEvent.SeatReservationCancelled;
import workshop.cinema.reservation.domain.ShowEvent.SeatReserved;
import workshop.cinema.reservation.domain.ShowEvent.ShowCreated;

import java.util.concurrent.CompletionStage;

public class ShowViewProjection {

    private final ShowViewRepository showViewRepository;
    private final ActorSystem<?> actorSystem;
    private final EventsByPersistenceIdQuery byPersistenceIdQuery;

    public ShowViewProjection(EventsByPersistenceIdQuery byPersistenceIdQuery, ActorSystem<?> actorSystem, ShowViewRepository showViewRepository) {
        this.byPersistenceIdQuery = byPersistenceIdQuery;
        this.actorSystem = actorSystem;
        this.showViewRepository = showViewRepository;
    }

    public CompletionStage<Done> run(String persistenceId) {
        long from = 0;
        long to = Long.MAX_VALUE;
        return byPersistenceIdQuery.eventsByPersistenceId(persistenceId, from, to)
                .mapAsync(1, this::processEvent)
                .run(actorSystem);
    }

    private CompletionStage<Done> processEvent(EventEnvelope eventEnvelope) {
        if (eventEnvelope.event() instanceof ShowEvent showEvent) {
            return switch (showEvent) {
                case ShowCreated showCreated -> showViewRepository.save(showCreated.showId(), showCreated.initialShow().seats().size());
                case SeatReserved seatReserved -> showViewRepository.decrementAvailability(seatReserved.showId());
                case SeatReservationCancelled seatReservationCancelled -> showViewRepository.incrementAvailability(seatReservationCancelled.showId());
            };
        } else {
            throw new IllegalStateException("Unrecognized event type");
        }
    }


}
