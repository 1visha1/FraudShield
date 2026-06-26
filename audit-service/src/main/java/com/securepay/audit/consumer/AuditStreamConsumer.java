package com.securepay.audit.consumer;

import com.securepay.audit.model.AuditDocument;
import com.securepay.audit.service.ElasticAuditService;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.connection.stream.ReadOffset;
import org.springframework.data.redis.connection.stream.StreamOffset;
import org.springframework.data.redis.connection.stream.StreamReadOptions;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Component
@RequiredArgsConstructor
@Slf4j
public class AuditStreamConsumer {

    private static final String STREAM_NAME = "audit-stream";

    private final RedisTemplate<String, Object> redisTemplate;
    private final ElasticAuditService elasticService;

    private final ExecutorService executor =
            Executors.newSingleThreadExecutor();

    private volatile boolean running = true;

    /**
     * Start reading from the beginning.
     * In production, store this offset externally (Redis consumer groups or DB).
     */
    private String lastRecordId = "0-0";

    @PostConstruct
    public void start() {

        log.info("Starting Redis Stream Consumer...");

        executor.submit(this::consume);
    }

    @PreDestroy
    public void stop() {

        log.info("Stopping Redis Stream Consumer...");

        running = false;

        executor.shutdownNow();
    }

    private void consume() {

        while (running) {

            try {

                Boolean streamExists = redisTemplate.hasKey(STREAM_NAME);

                if (Boolean.FALSE.equals(streamExists)) {

                    log.info("Waiting for Redis Stream '{}'...", STREAM_NAME);

                    Thread.sleep(2000);

                    continue;
                }

                List<MapRecord<String, Object, Object>> messages =
                        redisTemplate.opsForStream().read(
                                StreamReadOptions.empty()
                                        .count(10)
                                        .block(Duration.ofSeconds(2)),
                                StreamOffset.create(
                                        STREAM_NAME,
                                        ReadOffset.from(lastRecordId)
                                )
                        );

                if (messages == null || messages.isEmpty()) {
                    continue;
                }

                for (MapRecord<String, Object, Object> message : messages) {

                    Map<Object, Object> values = message.getValue();

                    log.info("Received Redis Stream Record: id={}, values={}",
                            message.getId().getValue(),
                            values);

                    AuditDocument document =
                            AuditDocument.builder()
                                    .eventType(String.valueOf(values.get("eventType")))
                                    .serviceName(String.valueOf(values.get("serviceName")))
                                    .transactionId(String.valueOf(values.get("transactionId")))
                                    .payload(String.valueOf(values.get("payload")))
                                    .timestamp(String.valueOf(values.get("timestamp")))
                                    .build();

                    elasticService.index(document);

                    log.info("Indexed Audit Event: {}", document.getEventType());

                    // Advance offset after successful processing
                    lastRecordId = message.getId().getValue();
                }

            } catch (InterruptedException ex) {

                Thread.currentThread().interrupt();
                break;

            } catch (Exception ex) {

                log.error("Redis Stream read/index failed.", ex);

                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }

        log.info("Redis Stream Consumer stopped.");
    }
}