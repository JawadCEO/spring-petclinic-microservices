package org.springframework.samples.petclinic.alerts.events;

import java.sql.SQLException;

import org.junit.jupiter.api.Test;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.kafka.listener.ListenerExecutionFailedException;
import org.springframework.transaction.CannotCreateTransactionException;

import static org.assertj.core.api.Assertions.assertThat;

class KafkaErrorConfigTest {

    @Test
    void recognisesADatabaseFailureInsideTheListenerException() {
        assertThat(KafkaErrorConfig.isDatabaseFailure(new ListenerExecutionFailedException("listener",
            new DataAccessResourceFailureException("connection refused")))).isTrue();
        assertThat(KafkaErrorConfig.isDatabaseFailure(new ListenerExecutionFailedException("listener",
            new CannotCreateTransactionException("no connection")))).isTrue();
        assertThat(KafkaErrorConfig.isDatabaseFailure(new RuntimeException(new SQLException("gone")))).isTrue();
    }

    @Test
    void aBadEventIsNotADatabaseFailure() {
        assertThat(KafkaErrorConfig.isDatabaseFailure(new ListenerExecutionFailedException("listener",
            new NullPointerException("petId")))).isFalse();
    }
}
