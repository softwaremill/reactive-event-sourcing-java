package workshop.cinema.reservation.api;

import java.util.UUID;

public record CreateShowRequest(UUID showId, String title, int maxSeats) {
}
