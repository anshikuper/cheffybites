package com.cheffybites.foundation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.github.f4b6a3.uuid.UuidCreator;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

@Testcontainers
class PostgisContainerCompatibilityIT {

    private static final String IMAGE = System.getProperty(
            "cheffy.test.postgis-image",
            "postgis/postgis:16-3.5@sha256:94146ac37bc61e2322f88016056c5920729cb8c64c8542ed590af8fc2abdac07"
    );

    @Container
    static final PostgreSQLContainer postgres = new PostgreSQLContainer(
            DockerImageName.parse(IMAGE).asCompatibleSubstituteFor("postgres")
    )
            .withDatabaseName("cheffy_foundation_test")
            .withUsername("cheffy")
            .withPassword("cheffy");

    @Test
    void supportsExtensionsAndHalfOpenGistExclusion() throws Exception {
        try (Connection connection = connection(); Statement statement = connection.createStatement()) {
            statement.execute("CREATE EXTENSION IF NOT EXISTS btree_gist");

            try (ResultSet versions = statement.executeQuery("""
                    SELECT current_setting('server_version'),
                           postgis_lib_version(),
                           (SELECT extversion FROM pg_extension WHERE extname = 'btree_gist')
                    """)) {
                assertThat(versions.next()).isTrue();
                assertThat(versions.getString(1)).startsWith("16.");
                assertThat(versions.getString(2)).startsWith("3.5.");
                assertThat(versions.getString(3)).startsWith("1.7");
            }

            statement.execute("""
                    CREATE TABLE gist_probe (
                        id uuid PRIMARY KEY,
                        resource_id uuid NOT NULL,
                        occupied_during tstzrange NOT NULL,
                        EXCLUDE USING gist (resource_id WITH =, occupied_during WITH &&)
                    )
                    """);

            UUID resourceId = UuidCreator.getTimeOrderedEpoch();
            try (PreparedStatement insert = connection.prepareStatement(
                    "INSERT INTO gist_probe (id, resource_id, occupied_during) "
                            + "VALUES (?, ?, tstzrange(?::timestamptz, ?::timestamptz, '[)'))")) {
                insert(insert, UuidCreator.getTimeOrderedEpoch(), resourceId,
                        "2026-08-27T14:00:00Z", "2026-08-27T15:00:00Z");
                insert(insert, UuidCreator.getTimeOrderedEpoch(), resourceId,
                        "2026-08-27T15:00:00Z", "2026-08-27T16:00:00Z");

                assertThatThrownBy(() -> insert(insert, UuidCreator.getTimeOrderedEpoch(), resourceId,
                        "2026-08-27T14:30:00Z", "2026-08-27T14:45:00Z"))
                        .isInstanceOf(SQLException.class)
                        .extracting(throwable -> ((SQLException) throwable).getSQLState())
                        .isEqualTo("23P01");
            }

            try (ResultSet count = statement.executeQuery("SELECT count(*) FROM gist_probe")) {
                assertThat(count.next()).isTrue();
                assertThat(count.getLong(1)).isEqualTo(2L);
            }
        }
    }

    protected static Connection connection() throws SQLException {
        return java.sql.DriverManager.getConnection(
                postgres.getJdbcUrl(),
                postgres.getUsername(),
                postgres.getPassword()
        );
    }

    private static void insert(
            PreparedStatement statement,
            UUID id,
            UUID resourceId,
            String startsAt,
            String endsAt
    ) throws SQLException {
        statement.setObject(1, id);
        statement.setObject(2, resourceId);
        statement.setString(3, startsAt);
        statement.setString(4, endsAt);
        statement.executeUpdate();
    }
}
