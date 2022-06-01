package workshop.cinema.base;

import akka.actor.typed.ActorSystem;
import akka.actor.typed.SpawnProtocol;
import akka.cluster.sharding.typed.javadsl.ClusterSharding;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import org.springframework.boot.web.embedded.netty.NettyServerCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import workshop.cinema.base.application.SpawningBehavior;
import workshop.cinema.base.domain.Clock;

import java.util.function.Function;

@Configuration
public class BaseConfiguration {

    @Bean
    public Config config() {
        return ConfigFactory.load();
    }

    @Bean(destroyMethod = "terminate")
    public ActorSystem<SpawnProtocol.Command> actorSystem(Config config) {
        return ActorSystem.create(SpawningBehavior.create(), "es-workshop", config);
    }

    @Bean
    public ClusterSharding clusterSharding(ActorSystem<?> actorSystem) {
        return ClusterSharding.get(actorSystem);
    }

    @Bean
    Clock clock() {
        return new Clock.UtcClock();
    }

    @Bean
    public NettyServerCustomizer nettyServerCustomizer() {
        return httpServer -> httpServer.metrics(true, Function.identity());
    }
}
