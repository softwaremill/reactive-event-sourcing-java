package workshop.cinema.reservation.infrastructure;

import akka.Done;
import workshop.cinema.reservation.application.projection.ShowViewRepository;
import workshop.cinema.reservation.application.projection.ShowView;
import workshop.cinema.reservation.domain.ShowId;

import java.util.List;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import static java.util.concurrent.CompletableFuture.completedFuture;
import static java.util.concurrent.CompletableFuture.supplyAsync;

public class InMemoryShowViewRepository implements ShowViewRepository {

    private ConcurrentMap<ShowId, ShowView> store = new ConcurrentHashMap<>();


    @Override
    public CompletionStage<List<ShowView>> findAvailable() {
        return completedFuture(store.values().stream().filter(showView -> showView.availableSeats() > 0).toList());
    }

    @Override
    public CompletionStage<Done> save(ShowId showId, int availableSeats) {
        return supplyAsync(() -> {
            store.put(showId, new ShowView(showId.toString(), availableSeats));
            return Done.done();
        });
    }

    @Override
    public CompletionStage<Done> decrementAvailability(ShowId showId) {
        return supplyAsync(() -> {
            store.compute(showId, (id, view) -> new ShowView(view.showId(), view.availableSeats() - 1));
            return Done.done();
        });
    }

    @Override
    public CompletionStage<Done> incrementAvailability(ShowId showId) {
        return supplyAsync(() -> {
            store.compute(showId, (id, view) -> new ShowView(view.showId(), view.availableSeats() - 2));
            return Done.done();
        });
    }
}
