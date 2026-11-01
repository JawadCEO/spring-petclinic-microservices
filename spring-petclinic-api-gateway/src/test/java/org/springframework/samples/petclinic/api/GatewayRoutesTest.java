package org.springframework.samples.petclinic.api;

import java.util.Map;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.gateway.config.GatewayProperties;
import org.springframework.cloud.gateway.route.RouteDefinition;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
class GatewayRoutesTest {

    @Autowired
    GatewayProperties gateway;

    @Test
    void aBookingIsNeverRetried() {
        assertThat(gateway.getDefaultFilters()).noneMatch(filter -> filter.getName().equals("Retry"));
        assertThat(hasRetry("visits-service")).isFalse();
    }

    @Test
    void theOtherRoutesKeepTheirRetry() {
        assertThat(hasRetry("vets-service")).isTrue();
        assertThat(hasRetry("customers-service")).isTrue();
        assertThat(hasRetry("genai-service")).isTrue();
    }

    private boolean hasRetry(String routeId) {
        Map<String, RouteDefinition> routes = gateway.getRoutes().stream()
            .collect(Collectors.toMap(RouteDefinition::getId, route -> route));
        return routes.get(routeId).getFilters().stream().anyMatch(filter -> filter.getName().equals("Retry"));
    }
}
