package workshop.cinema.reservation.application.projection;

import akka.Done;
import akka.projection.eventsourced.EventEnvelope;
import akka.projection.javadsl.Handler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import workshop.cinema.reservation.domain.ShowEvent;

import java.util.concurrent.CompletionStage;

public class ShowViewEventHandler extends Handler<EventEnvelope<ShowEvent>> {

    private final Logger log = LoggerFactory.getLogger(getClass());
    private final ShowViewRepository showViewRepository;

    public ShowViewEventHandler(ShowViewRepository showViewRepository) {
        this.showViewRepository = showViewRepository;
    }

    @Override
    public CompletionStage<Done> process(EventEnvelope<ShowEvent> showEventEventEnvelope) {
        log.info("Processing: {}", showEventEventEnvelope.event());
        return switch (showEventEventEnvelope.event()) {
            case ShowEvent.ShowCreated showCreated ->
                    showViewRepository.save(showCreated.showId(), showCreated.initialShow().seats().size());
            case ShowEvent.SeatReserved seatReserved -> showViewRepository.decrementAvailability(seatReserved.showId());
            case ShowEvent.SeatReservationCancelled seatReservationCancelled ->
                    showViewRepository.incrementAvailability(seatReservationCancelled.showId());
        };
    }
}
