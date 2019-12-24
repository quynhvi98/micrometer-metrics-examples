package com.mservice.micrometer.prometheus;

import com.sun.net.httpserver.HttpServer;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Meter;
import io.micrometer.core.instrument.Metrics;
import io.micrometer.core.instrument.Timer;
import io.micrometer.core.instrument.binder.jvm.JvmMemoryMetrics;
import io.micrometer.core.instrument.binder.jvm.JvmThreadMetrics;
import io.micrometer.core.instrument.binder.system.ProcessorMetrics;
import io.micrometer.core.instrument.binder.system.UptimeMetrics;
import io.micrometer.core.instrument.config.MeterFilter;
import io.micrometer.core.instrument.distribution.DistributionStatisticConfig;
import io.micrometer.prometheus.PrometheusConfig;
import io.micrometer.prometheus.PrometheusMeterRegistry;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.concurrent.TimeUnit;


public class PrometheusMain {

    public static void main(String[] args) throws InterruptedException {

        PrometheusMeterRegistry prometheusRegistry = new PrometheusMeterRegistry(PrometheusConfig.DEFAULT);
        Metrics.addRegistry(prometheusRegistry);
        try {
            HttpServer server = HttpServer.create(new InetSocketAddress(9300), 0);
            server.createContext("/prometheus", httpExchange -> {
                String response = prometheusRegistry.scrape();
                httpExchange.sendResponseHeaders(200, response.getBytes().length);
                try (OutputStream os = httpExchange.getResponseBody()) {
                    os.write(response.getBytes());
                }
            });
            new Thread(server::start).start();

            // Meter Binders
            new JvmMemoryMetrics().bindTo(Metrics.globalRegistry);
            new ProcessorMetrics().bindTo(Metrics.globalRegistry);
            new JvmThreadMetrics().bindTo(Metrics.globalRegistry);
            UptimeMetrics uptimeMetrics = new UptimeMetrics();
            uptimeMetrics.bindTo(Metrics.globalRegistry);

            // Counters
            Counter.builder("counter.message")
                    .description("Counter message")
                    .tag("Message", "My message")
                    .register(Metrics.globalRegistry).increment();

            //turn on client-side percentiles for both timers via a meter filter
            Metrics.globalRegistry.config().meterFilter(new MeterFilter() {
                @Override
                public DistributionStatisticConfig configure(Meter.Id id, DistributionStatisticConfig config) {
                    if (id.getName().startsWith("my.timer.sample")) {
                        return DistributionStatisticConfig.builder()
                                .percentilesHistogram(true)
                                .build()
                                .merge(config);
                    }
                    return config;
                }
            });

            //Timer
            long begin = System.currentTimeMillis();
            Thread.sleep(3000);
            long duration = System.currentTimeMillis() - begin;
            Timer.builder("my.timer.processing.time")
                    .description("Timer processing time") // optional
                    .tags("Result", "Success") // optional
                    .register(Metrics.globalRegistry)
                    .record(duration, TimeUnit.MILLISECONDS);

            //Storing start state in Timer.Sample
            Timer.Sample sample = Timer.start(Metrics.globalRegistry);
            Thread.sleep(4000);
            sample.stop(Timer.builder("my.timer.sample.processing.time")
                    .description("Timer sample processing time")// optional
                    .tags("Result", "Success")// optional
                    .register(Metrics.globalRegistry));

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
