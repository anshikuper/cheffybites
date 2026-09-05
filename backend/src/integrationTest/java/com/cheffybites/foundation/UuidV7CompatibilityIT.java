package com.cheffybites.foundation;

import static org.assertj.core.api.Assertions.assertThat;

import com.github.f4b6a3.uuid.UuidCreator;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class UuidV7CompatibilityIT extends PostgisContainerCompatibilityIT {

    @Test
    void producesRfc9562ValuesAndRoundTripsThroughPostgresql() throws Exception {
        Set<UUID> ids = new HashSet<>();
        for (int index = 0; index < 10_000; index++) {
            UUID id = UuidCreator.getTimeOrderedEpoch();
            assertThat(id.version()).isEqualTo(7);
            assertThat(id.variant()).isEqualTo(2);
            assertThat(ids.add(id)).isTrue();
        }

        UUID expected = ids.iterator().next();
        try (Connection connection = connection();
             PreparedStatement query = connection.prepareStatement("SELECT ?::uuid")) {
            query.setObject(1, expected);
            try (ResultSet result = query.executeQuery()) {
                assertThat(result.next()).isTrue();
                assertThat(result.getObject(1, UUID.class)).isEqualTo(expected);
            }
        }
    }
}
