package org.springframework.samples.petclinic.alerts.events;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Reads the shared examples in {@code contracts/examples}, the same files the producer's format is
 * checked against, so the consumer's copy of the event cannot drift from the contract unnoticed.
 */
class VisitEventContractTest {

    // Maven runs tests from the module directory
    private static final Path EXAMPLES = Path.of("..", "contracts", "examples");

    static List<Path> validExamples() throws IOException {
        try (Stream<Path> files = Files.list(EXAMPLES)) {
            return files.filter(file -> file.getFileName().toString().startsWith("valid-")).toList();
        }
    }

    @ParameterizedTest
    @MethodSource("validExamples")
    void readsEveryValidExample(Path example) throws IOException {
        VisitEvent event = VisitEventListener.parse(Files.readString(example));

        assertThat(event.eventId()).isNotNull();
        assertThat(event.visitDate()).isNotNull();
        assertThat(event.occurredAt()).isNotNull();
    }

    @Test
    void thereAreValidExamples() throws IOException {
        assertThat(validExamples()).isNotEmpty();
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "invalid-02-missing-event-id.json",
        "invalid-03-malformed-uuid.json",
        "invalid-04-bad-date-format.json",
        "invalid-05-unknown-event-type.json",
        "invalid-06-missing-pet-id.json"
    })
    void refusesAnEventItCouldNotStore(String example) throws IOException {
        String message = Files.readString(EXAMPLES.resolve(example));

        assertThatThrownBy(() -> VisitEventListener.parse(message)).isInstanceOf(Exception.class);
    }
}
