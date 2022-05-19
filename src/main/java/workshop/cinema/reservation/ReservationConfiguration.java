package workshop.cinema.reservation;

import akka.actor.typed.ActorSystem;
import akka.actor.typed.SpawnProtocol;
import akka.cluster.sharding.typed.javadsl.ClusterSharding;
import akka.persistence.jdbc.query.javadsl.JdbcReadJournal;
import akka.persistence.query.Offset;
import akka.projection.eventsourced.EventEnvelope;
import akka.projection.eventsourced.javadsl.EventSourcedProvider;
import akka.projection.javadsl.SourceProvider;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import workshop.cinema.base.domain.Clock;
import workshop.cinema.reservation.application.ShowEntity;
import workshop.cinema.reservation.application.ShowService;
import workshop.cinema.reservation.application.projection.ProjectionLauncher;
import workshop.cinema.reservation.application.projection.ShowViewEventHandler;
import workshop.cinema.reservation.application.projection.ShowViewProjection;
import workshop.cinema.reservation.application.projection.ShowViewRepository;
import workshop.cinema.reservation.domain.ShowEvent;
import workshop.cinema.reservation.infrastructure.InMemoryShowViewRepository;

import javax.sql.DataSource;

@Configuration
public class ReservationConfiguration {

    private final ActorSystem<SpawnProtocol.Command> system;
    private final ClusterSharding sharding;

    public ReservationConfiguration(ActorSystem<SpawnProtocol.Command> system, ClusterSharding sharding, Clock clock) {
        this.system = system;
        this.sharding = sharding;
        this.clock = clock;
    }

    private final Clock clock;


    @Bean
    public ShowService showService() {
        return new ShowService(sharding, clock);
    }

    @Bean
    public ShowViewRepository showViewRepository() {
        return new InMemoryShowViewRepository();
    }

    @Bean(initMethod = "runProjections")
    public ProjectionLauncher projectionLauncher(ShowViewRepository showViewRepository) {
        ShowViewEventHandler showViewEventHandler = new ShowViewEventHandler(showViewRepository);
        SourceProvider<Offset, EventEnvelope<ShowEvent>> sourceProvider = EventSourcedProvider.eventsByTag(system, JdbcReadJournal.Identifier(), ShowEntity.SHOW_EVENT_TAG);
        ShowViewProjection showViewProjection = new ShowViewProjection(system, dataSource(), showViewEventHandler);
        ProjectionLauncher projectionLauncher = new ProjectionLauncher(system);
        projectionLauncher.withLocalProjections(showViewProjection.create(sourceProvider));
        return projectionLauncher;
    }

    public DataSource dataSource() {
        var hikariDataSource = new HikariDataSource();
        hikariDataSource.setPoolName("projection-data-source");
        hikariDataSource.setJdbcUrl("jdbc:postgresql://localhost:5432/postgres");
        hikariDataSource.setUsername("admin");
        hikariDataSource.setPassword("admin");
        // https://github.com/brettwooldridge/HikariCP/wiki/About-Pool-Sizing
        hikariDataSource.setMaximumPoolSize(5);
        hikariDataSource.setRegisterMbeans(true);
        return hikariDataSource;
    }
}
