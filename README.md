# micrometer-metrics-examples

# Micrometer Metrics examples

### Build the example from the command line

```
mvn clean package
```

### Prometheus

#### Run package
```
java -jar prometheus/target/prometheus-1.0.jar
```

#### Check metrics data
HTTP server implementation to expose scrape data to Prometheus

Access : http://localhost:9300/prometheus

Note: https://prometheus.io/docs/prometheus/latest/getting_started/ for Prometheus getting started guide. Besides, you can pull prometheus image from docker hub (https://hub.docker.com/r/prom/prometheus/)
You need to configure the Prometheus server to scrape localhost:9300

```
vi /etc/prometheus/prometheus.yml
```

```
- job_name: 'metrics'
    metrics_path: /prometheus
    static_configs:
       - targets: ['localhost:9300']
```

 
```
killall -HUP prometheus
```

#### View in Grafana
Note: https://grafana.com/docs/ for Grafana getting started guide. Besides, you can pull Grafana image from docker hub (https://hub.docker.com/r/grafana/grafana/)


### BeeInstant

#### Run package
```
java -Dmanagement.metrics.export.statsd.enabled=true -Dmanagement.metrics.export.statsd.host=localhost -Dmanagement.metrics.export.statsd.port=8125 -Dmanagement.metrics.export.statsd.flavor=telegraf -jar bee-instant/target/bee-instant-1.0.jar
```

Note: https://app.beeinstant.com/login?backPage=%2Fdashboard for install BeeAgent . Besides, you can pull BeeInstant image from docker hub (https://hub.docker.com/r/beeinstant/statsbee)

Enable debug log, add the line _debug=true

```
vi /etc/statsbee/statsbee.conf 
```
```
tail -f /tmp/statsbee.INFO
```
