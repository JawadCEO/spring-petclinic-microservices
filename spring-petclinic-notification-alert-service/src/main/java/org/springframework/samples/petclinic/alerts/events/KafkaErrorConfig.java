package org.springframework.samples.petclinic.alerts.events;

import java.sql.SQLException;
import java.time.Duration;

import org.apache.kafka.common.TopicPartition;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.dao.DataAccessException;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.transaction.TransactionException;
import org.springframework.util.backoff.BackOff;
import org.springframework.util.backoff.FixedBackOff;

@Configuration
public class KafkaErrorConfig {

    /**
     * Retries a failing event, then moves it to the dead-letter topic so that it stops blocking
     * the events behind it. Spring Boot gives this bean to the listener container.
     * <p>
     * A bad event will never succeed, so it gets the short retry. A database failure says nothing
     * about the event, so it waits much longer for the database to come back: holding the partition
     * during an outage keeps good events in order and out of the dead-letter topic.
     */
    @Bean
    DefaultErrorHandler visitEventErrorHandler(
        KafkaTemplate<?, ?> kafkaTemplate,
        @Value("${petclinic.events.dead-letter-topic}") String deadLetterTopic,
        @Value("${petclinic.events.retries}") long retries,
        @Value("${petclinic.events.retry-interval}") Duration interval,
        @Value("${petclinic.events.database-retries}") long databaseRetries,
        @Value("${petclinic.events.database-retry-interval}") Duration databaseInterval) {

        // The dead-letter topic has a single partition, unlike visit-events
        DeadLetterPublishingRecoverer recoverer = new DeadLetterPublishingRecoverer(kafkaTemplate,
            (record, failure) -> new TopicPartition(deadLetterTopic, 0));
        DefaultErrorHandler handler = new DefaultErrorHandler(recoverer, new FixedBackOff(interval.toMillis(), retries));
        BackOff databaseBackOff = new FixedBackOff(databaseInterval.toMillis(), databaseRetries);
        handler.setBackOffFunction((record, failure) -> isDatabaseFailure(failure) ? databaseBackOff : null);
        return handler;
    }

    static boolean isDatabaseFailure(Throwable failure) {
        for (Throwable cause = failure; cause != null; cause = cause.getCause()) {
            if (cause instanceof DataAccessException || cause instanceof TransactionException
                || cause instanceof SQLException) {
                return true;
            }
        }
        return false;
    }
}
