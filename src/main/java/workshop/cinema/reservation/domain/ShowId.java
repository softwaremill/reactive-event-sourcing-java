package workshop.cinema.reservation.domain;

import java.io.Serializable;
import java.util.UUID;

public record ShowId(UUID id) implements Serializable {

    public static ShowId of() {
        return of(UUID.randomUUID());
    }

    public static ShowId of(UUID showId) {
        return new ShowId(showId);
    }
}
