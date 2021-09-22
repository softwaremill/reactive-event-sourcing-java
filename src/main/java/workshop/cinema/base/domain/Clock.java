package workshop.cinema.base.domain;

import java.time.Instant;

public interface Clock {
    Instant now();

    class UtcClock implements Clock {
        @Override
        public Instant now() {
            return Instant.now();
        }
    }
}
