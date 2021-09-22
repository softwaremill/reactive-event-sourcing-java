package workshop.cinema.reservation.domain;

import java.math.BigDecimal;
import java.util.Random;
import java.util.UUID;

import static workshop.cinema.reservation.domain.ShowBuilder.showBuilder;

public class DomainGenerators {

    private static Random random = new Random();

    public static ShowId randomShowId() {
        return new ShowId(UUID.randomUUID());
    }

    public static BigDecimal randomPrice() {
        return BigDecimal.valueOf(random.nextInt(200) + 50);
    }

    public static String randomTitle() {
        return "show-title-" + UUID.randomUUID().toString().substring(0, 8);
    }

    public static SeatNumber randomSeatNumber() {
        return new SeatNumber(SeatsCreator.SEAT_RANGE.get(random.nextInt(SeatsCreator.SEAT_RANGE.size())));
    }

    public static Show randomShow() {
        return showBuilder().withRandomSeats().build();
    }
}
