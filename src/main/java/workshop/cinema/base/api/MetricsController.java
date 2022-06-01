package workshop.cinema.base.api;

import io.prometheus.client.CollectorRegistry;
import io.prometheus.client.exporter.common.TextFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.io.StringWriter;

@RestController
@RequestMapping(value = "/metrics")
public class MetricsController {

    private final CollectorRegistry collectorRegistry;

    public MetricsController(CollectorRegistry collectorRegistry) {
        this.collectorRegistry = collectorRegistry;
    }

    @GetMapping(produces = "text/plain")
    public Mono<String> metrics() {
        return Mono.fromCallable(() -> {
            var writer = new StringWriter();
            TextFormat.write004(writer, collectorRegistry.metricFamilySamples());
            return writer.toString();
        });
    }
}
