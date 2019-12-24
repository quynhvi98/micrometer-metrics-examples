package com.mservice.micrometer.bee;

import com.sun.net.httpserver.HttpServer;
import io.micrometer.core.instrument.Clock;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Metrics;
import io.micrometer.core.instrument.Timer;
import io.micrometer.core.instrument.binder.jvm.JvmMemoryMetrics;
import io.micrometer.core.instrument.binder.jvm.JvmThreadMetrics;
import io.micrometer.core.instrument.binder.system.ProcessorMetrics;
import io.micrometer.core.instrument.binder.system.UptimeMetrics;
import io.micrometer.statsd.StatsdConfig;
import io.micrometer.statsd.StatsdFlavor;
import io.micrometer.statsd.StatsdMeterRegistry;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.concurrent.TimeUnit;

public class BeeMain {
    public static void main(String[] args) throws InterruptedException, IOException {

        HttpServer server = HttpServer.create(new InetSocketAddress(8000), 0);
        server.createContext("/bee", handler -> {
            String response = "This is the response";
            handler.sendResponseHeaders(200, response.length());
            OutputStream os = handler.getResponseBody();
            os.write(response.getBytes());
            os.close();
        });
        server.setExecutor(null);
        server.start();


        StatsdConfig statsdConfig = new StatsdConfig() {
            public String get(String s) {
                return null;
            }

            public StatsdFlavor flavor() {
                return StatsdFlavor.TELEGRAF;
            }
        };

        StatsdMeterRegistry statsdMeterRegistry = new StatsdMeterRegistry(statsdConfig, Clock.SYSTEM);
        Metrics.addRegistry(statsdMeterRegistry);

        // Meter Binders
        new JvmMemoryMetrics().bindTo(Metrics.globalRegistry);
        new ProcessorMetrics().bindTo(Metrics.globalRegistry);
        new JvmThreadMetrics().bindTo(Metrics.globalRegistry);
        UptimeMetrics uptimeMetrics = new UptimeMetrics();
        uptimeMetrics.bindTo(Metrics.globalRegistry);

        // Counters
        Counter.builder("counter.message.bee")
                .description("Counter message for Bee")
                .tag("Message", "My message")
                .register(Metrics.globalRegistry).increment();

        //Timer
        long begin = System.currentTimeMillis();
        Thread.sleep(3000);
        long duration = System.currentTimeMillis() - begin;
        Timer.builder("my.timer.processing.time.bee")
                .description("Timer processing time for Bee") // optional
                .tags("Result", "Success") // optional
                .register(Metrics.globalRegistry)
                .record(duration, TimeUnit.MILLISECONDS);

        //Storing start state in Timer.Sample
        Timer.Sample sample = Timer.start(Metrics.globalRegistry);
        Thread.sleep(4000);
        sample.stop(Timer.builder("my.timer.sample.processing.time.bee")
                .description("Timer sample processing time for bee")// optional
                .tags("Result", "Success")// optional
                .register(Metrics.globalRegistry));


    }
}
