package workshop.cinema.reservation.application;

import akka.cluster.sharding.typed.javadsl.ClusterSharding;
import akka.cluster.sharding.typed.javadsl.Entity;
import akka.cluster.sharding.typed.javadsl.EntityRef;
import workshop.cinema.base.domain.Clock;
import workshop.cinema.reservation.domain.SeatNumber;
import workshop.cinema.reservation.domain.Show;
import workshop.cinema.reservation.domain.ShowCommand;
import workshop.cinema.reservation.domain.ShowCommand.CancelSeatReservation;
import workshop.cinema.reservation.domain.ShowCommand.ReserveSeat;
import workshop.cinema.reservation.domain.ShowId;

import java.time.Duration;
import java.util.UUID;
import java.util.concurrent.CompletionStage;

import static workshop.cinema.reservation.application.ShowEntity.SHOW_ENTITY_TYPE_KEY;

public class ShowService {

    private final ClusterSharding sharding;
    private Duration askTimeout = Duration.ofSeconds(2); //TODO should be configurable

    public ShowService(ClusterSharding sharding, Clock clock) {
        this.sharding = sharding;
        sharding.init(Entity.of(SHOW_ENTITY_TYPE_KEY, entityContext -> {
            ShowId showId = new ShowId(UUID.fromString(entityContext.getEntityId()));
            return ShowEntity.create(showId, clock);
        }));
    }

    public CompletionStage<Show> findShowBy(ShowId showId) {
        return getShowEntityRef(showId).ask(replyTo -> new ShowEntityCommand.GetShow(replyTo), askTimeout);
    }

    public CompletionStage<ShowEntityResponse> reserveSeat(ShowId showId, SeatNumber seatNumber) {
        return processCommand(new ReserveSeat(showId, seatNumber));
    }

    public CompletionStage<ShowEntityResponse> cancelReservation(ShowId showId, SeatNumber seatNumber) {
        return processCommand(new CancelSeatReservation(showId, seatNumber));
    }

    private CompletionStage<ShowEntityResponse> processCommand(ShowCommand showCommand) {
        return getShowEntityRef(showCommand.showId())
                .ask(replyTo -> new ShowEntityCommand.ShowCommandEnvelope(showCommand, replyTo), askTimeout);
    }

    private EntityRef<ShowEntityCommand> getShowEntityRef(ShowId showId) {
        return sharding.entityRefFor(SHOW_ENTITY_TYPE_KEY, showId.id().toString());
    }
}
