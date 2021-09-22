package workshop.cinema.reservation.domain;

import workshop.cinema.base.domain.Clock;

import java.time.Instant;

public class FixedClock implements Clock {
    private final Instant now;

    public FixedClock(Instant now) {
        this.now = now;
    }

    public FixedClock() {
        this(Instant.now());
    }

    @Override
    public Instant now() {
        return now;
    }
}
