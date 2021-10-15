package workshop.cinema.reservation;

import akka.cluster.sharding.typed.javadsl.ClusterSharding;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import workshop.cinema.base.domain.Clock;
import workshop.cinema.reservation.application.ShowService;

@Configuration
public class ReservationConfiguration {

    @Bean
    public ShowService showService(ClusterSharding sharding, Clock clock) {
        return new ShowService(sharding, clock);
    }
}
