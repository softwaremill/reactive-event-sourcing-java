package workshop.cinema.base;

import akka.actor.typed.ActorSystem;
import akka.cluster.sharding.typed.javadsl.ClusterSharding;
import akka.persistence.testkit.PersistenceTestKitPlugin;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import workshop.cinema.base.application.VoidBehavior;
import workshop.cinema.base.domain.Clock;

@Configuration
public class BaseConfiguration {

    @Bean
    public Config config() {
        return PersistenceTestKitPlugin.config().withFallback(ConfigFactory.load());
    }

    @Bean(destroyMethod = "terminate")
    public ActorSystem<Void> actorSystem(Config config) {
        return ActorSystem.create(VoidBehavior.create(), "es-workshop", config);
    }

    @Bean
    public ClusterSharding clusterSharding(ActorSystem<?> actorSystem) {
        return ClusterSharding.get(actorSystem);
    }

    @Bean
    Clock clock() {
        return new Clock.UtcClock();
    }
}
