package com.cheffybites.foundation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.github.f4b6a3.uuid.UuidCreator;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Test;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

@Testcontainers
class FlywayFoundationIT {

    private static final String IMAGE = System.getProperty(
            "cheffy.test.postgis-image",
            "postgis/postgis:16-3.5@sha256:94146ac37bc61e2322f88016056c5920729cb8c64c8542ed590af8fc2abdac07"
    );

    @Container
    static final PostgreSQLContainer postgres = new PostgreSQLContainer(
            DockerImageName.parse(IMAGE).asCompatibleSubstituteFor("postgres")
    )
            .withDatabaseName("cheffy_migration_test")
            .withUsername("cheffy")
            .withPassword("cheffy");

    @Test
    void appliesAndVerifiesFoundationMigrations() throws Exception {
        Flyway.configure()
                .dataSource(postgres.getJdbcUrl(), postgres.getUsername(), postgres.getPassword())
                .locations("classpath:db/migration")
                .load()
                .migrate();

        try (Connection connection = connection()) {
            verifyMigrationHistory(connection);
            verifySchemasAndExtensions(connection);
            verifyPostgis(connection);
            verifyBtreeGist(connection);
            verifyPlatformTables(connection);
            verifyOutboxTableAndPersistence(connection);
            verifyUuidV7RoundTrip(connection);
        }
    }

    private static void verifyMigrationHistory(Connection connection) throws SQLException {
        try (PreparedStatement query = connection.prepareStatement(
                "SELECT version, description, success FROM flyway_schema_history ORDER BY installed_rank")) {
            try (ResultSet result = query.executeQuery()) {
                assertMigration(result, "1", "create phase1 schemas and extensions");
                assertMigration(result, "2", "create platform and outbox foundation");
                assertThat(result.next()).isFalse();
            }
        }
    }

    private static void assertMigration(ResultSet result, String version, String description) throws SQLException {
        assertThat(result.next()).isTrue();
        assertThat(result.getString("version")).isEqualTo(version);
        assertThat(result.getString("description")).isEqualTo(description);
        assertThat(result.getBoolean("success")).isTrue();
    }

    private static void verifySchemasAndExtensions(Connection connection) throws SQLException {
        Set<String> expectedSchemas = Set.of(
                "identity", "organization", "kitchen", "media", "equipment", "chef",
                "booking", "notification", "feedback", "platform", "outbox");
        try (PreparedStatement query = connection.prepareStatement(
                "SELECT schema_name FROM information_schema.schemata WHERE schema_name = ANY (?)")) {
            query.setArray(1, connection.createArrayOf("text", expectedSchemas.toArray()));
            try (ResultSet result = query.executeQuery()) {
                Set<String> actualSchemas = new HashSet<>();
                while (result.next()) {
                    actualSchemas.add(result.getString(1));
                }
                assertThat(actualSchemas).containsExactlyInAnyOrderElementsOf(expectedSchemas);
            }
        }

        try (PreparedStatement query = connection.prepareStatement(
                "SELECT extname FROM pg_extension WHERE extname IN ('postgis', 'btree_gist') ORDER BY extname")) {
            try (ResultSet result = query.executeQuery()) {
                assertThat(result.next()).isTrue();
                assertThat(result.getString(1)).isEqualTo("btree_gist");
                assertThat(result.next()).isTrue();
                assertThat(result.getString(1)).isEqualTo("postgis");
                assertThat(result.next()).isFalse();
            }
        }
    }

    private static void verifyPostgis(Connection connection) throws SQLException {
        try (Statement statement = connection.createStatement();
             ResultSet result = statement.executeQuery("SELECT ST_AsText(ST_SetSRID(ST_MakePoint(-79.38, 43.65), 4326))")) {
            assertThat(result.next()).isTrue();
            assertThat(result.getString(1)).isEqualTo("POINT(-79.38 43.65)");
        }
    }

    private static void verifyBtreeGist(Connection connection) throws SQLException {
        try (Statement statement = connection.createStatement()) {
            statement.execute("CREATE TEMP TABLE gist_probe (resource_id UUID NOT NULL, occupied_during TSTZRANGE NOT NULL)");
            statement.execute("CREATE INDEX gist_probe_resource_idx ON gist_probe USING GIST (resource_id)");
            statement.execute("INSERT INTO gist_probe VALUES ('018f0b8e-7b6d-7a5e-8e2f-111111111111', '[2026-08-27 14:00:00+00, 2026-08-27 15:00:00+00)'::tstzrange)");
            try (ResultSet result = statement.executeQuery(
                    "SELECT count(*) FROM gist_probe WHERE resource_id = '018f0b8e-7b6d-7a5e-8e2f-111111111111'")) {
                assertThat(result.next()).isTrue();
                assertThat(result.getLong(1)).isEqualTo(1L);
            }
        }
    }

    private static void verifyPlatformTables(Connection connection) throws SQLException {
        assertColumns(connection, "platform", "data_scopes", new String[][]{
                {"id", "uuid", "NO"}, {"environment_key", "text", "NO"}, {"record_mode", "text", "NO"},
                {"resettable", "boolean", "NO"}, {"status", "text", "NO"}, {"created_at", "timestamp with time zone", "NO"}
        });
        assertColumns(connection, "platform", "pilot_stage_state", new String[][]{
                {"singleton_id", "smallint", "NO"}, {"stage", "text", "NO"}, {"version", "integer", "NO"},
                {"changed_by_user_id", "uuid", "YES"}, {"change_reason", "text", "YES"}, {"changed_at", "timestamp with time zone", "NO"}
        });
        assertColumns(connection, "platform", "pilot_stage_history", new String[][]{
                {"id", "uuid", "NO"}, {"from_stage", "text", "YES"}, {"to_stage", "text", "NO"},
                {"changed_by_user_id", "uuid", "YES"}, {"change_reason", "text", "YES"}, {"changed_at", "timestamp with time zone", "NO"}
        });

        assertCheckConstraint(connection, "platform", "data_scopes", "record_mode", "demo", "real", "any");
        assertCheckConstraint(connection, "platform", "data_scopes", "resettable", "false", "record_mode", "demo", "or");
        assertCheckConstraint(connection, "platform", "pilot_stage_state", "singleton_id", "1");
        assertCheckConstraint(connection, "platform", "pilot_stage_state", "stage", "pre_pilot", "controlled_pilot", "any");
        assertCheckConstraint(connection, "platform", "pilot_stage_history", "from_stage", "isnull", "pre_pilot", "controlled_pilot", "any");
        assertCheckConstraint(connection, "platform", "pilot_stage_history", "to_stage", "pre_pilot", "controlled_pilot", "any");
        assertPrimaryKey(connection, "platform", "data_scopes", "id");
        assertPrimaryKey(connection, "platform", "pilot_stage_state", "singleton_id");
        assertPrimaryKey(connection, "platform", "pilot_stage_history", "id");
        assertUniqueConstraint(connection, "platform", "data_scopes", "environment_key", "id");
        assertThat(foreignKeyExists(connection, "platform", "pilot_stage_state", "changed_by_user_id")).isFalse();
        assertThat(foreignKeyExists(connection, "platform", "pilot_stage_history", "changed_by_user_id")).isFalse();

        try (PreparedStatement insert = connection.prepareStatement(
                "INSERT INTO platform.pilot_stage_state (singleton_id, stage, version, changed_at) VALUES (1, 'PRE_PILOT', 1, now())")) {
            insert.executeUpdate();
        }
        assertThatThrownBy(() -> connection.createStatement().executeUpdate(
                "INSERT INTO platform.pilot_stage_state (singleton_id, stage, version, changed_at) VALUES (2, 'PRE_PILOT', 1, now())"))
                .isInstanceOf(SQLException.class);
    }

    private static void verifyOutboxTableAndPersistence(Connection connection) throws SQLException {
        assertColumns(connection, "outbox", "outbox_events", new String[][]{
                {"id", "uuid", "NO"}, {"aggregate_type", "character varying", "NO"}, {"aggregate_id", "uuid", "NO"},
                {"event_type", "character varying", "NO"}, {"event_version", "integer", "NO"}, {"correlation_id", "uuid", "YES"},
                {"causation_id", "uuid", "YES"}, {"payload", "jsonb", "NO"}, {"occurred_at", "timestamp with time zone", "NO"},
                {"published_at", "timestamp with time zone", "YES"}, {"attempts", "integer", "NO"}, {"last_error", "text", "YES"},
                {"next_attempt_at", "timestamp with time zone", "YES"}, {"created_at", "timestamp with time zone", "NO"}
        });
            assertThat(columnLength(connection, "outbox", "outbox_events", "aggregate_type")).isEqualTo(100);
            assertThat(columnLength(connection, "outbox", "outbox_events", "event_type")).isEqualTo(200);
            assertThat(columnDefault(connection, "outbox", "outbox_events", "event_version"))
                .isEqualTo("1");
            assertThat(columnDefault(connection, "outbox", "outbox_events", "attempts"))
                .isEqualTo("0");
            assertThat(normalizeSql(columnDefault(connection, "outbox", "outbox_events", "occurred_at")))
                .isEqualTo("now");
            assertThat(normalizeSql(columnDefault(connection, "outbox", "outbox_events", "created_at")))
                .isEqualTo("now");
            assertPrimaryKey(connection, "outbox", "outbox_events", "id");
            assertIndex(connection, "outbox", "outbox_events", "idx_outbox_unpublished",
                new String[]{"published_at", "next_attempt_at"}, "published_at IS NULL");
            assertIndex(connection, "outbox", "outbox_events", "idx_outbox_aggregate",
                new String[]{"aggregate_type", "aggregate_id"}, null);
            assertIndex(connection, "outbox", "outbox_events", "idx_outbox_correlation",
                new String[]{"correlation_id"}, "correlation_id IS NOT NULL");

        UUID id = UuidCreator.getTimeOrderedEpoch();
        UUID aggregateId = UuidCreator.getTimeOrderedEpoch();
        Instant occurredAt = Instant.parse("2026-08-27T15:00:00Z");
        String payload = "{\"kind\":\"foundation-test\",\"value\":7}";
        try (PreparedStatement insert = connection.prepareStatement(
                "INSERT INTO outbox.outbox_events (id, aggregate_type, aggregate_id, event_type, payload, occurred_at) "
                        + "VALUES (?, 'FOUNDATION', ?, 'FoundationPersistenceTest.v1', ?::jsonb, ?)")) {
            insert.setObject(1, id);
            insert.setObject(2, aggregateId);
            insert.setString(3, payload);
            insert.setTimestamp(4, java.sql.Timestamp.from(occurredAt));
            insert.executeUpdate();
        }

        try (PreparedStatement query = connection.prepareStatement(
                "SELECT id, aggregate_id, jsonb_typeof(payload) AS payload_type, payload->>'kind' AS payload_kind, "
                    + "occurred_at, published_at, event_version, attempts, created_at "
                        + "FROM outbox.outbox_events WHERE id = ?")) {
            query.setObject(1, id);
            try (ResultSet result = query.executeQuery()) {
                assertThat(result.next()).isTrue();
                assertThat(result.getObject("id", UUID.class)).isEqualTo(id);
                assertThat(result.getObject("aggregate_id", UUID.class)).isEqualTo(aggregateId);
                assertThat(result.getString("payload_type")).isEqualTo("object");
                assertThat(result.getString("payload_kind")).isEqualTo("foundation-test");
                assertThat(result.getObject("occurred_at", java.time.OffsetDateTime.class).toInstant()).isEqualTo(occurredAt);
                assertThat(result.getObject("published_at")).isNull();
                assertThat(result.getInt("event_version")).isEqualTo(1);
                assertThat(result.getInt("attempts")).isZero();
                assertThat(result.getObject("created_at")).isNotNull();
            }
        }
    }

    private static void verifyUuidV7RoundTrip(Connection connection) throws SQLException {
        Set<UUID> ids = new HashSet<>();
        for (int index = 0; index < 1_000; index++) {
            UUID id = UuidCreator.getTimeOrderedEpoch();
            assertThat(id.version()).isEqualTo(7);
            assertThat(id.variant()).isEqualTo(2);
            assertThat(ids.add(id)).isTrue();
        }
        UUID expected = ids.iterator().next();
        try (PreparedStatement query = connection.prepareStatement("SELECT ?::uuid")) {
            query.setObject(1, expected);
            try (ResultSet result = query.executeQuery()) {
                assertThat(result.next()).isTrue();
                assertThat(result.getObject(1, UUID.class)).isEqualTo(expected);
            }
        }
    }

    private static void assertColumns(Connection connection, String schema, String table, String[][] expected)
            throws SQLException {
        try (PreparedStatement query = connection.prepareStatement(
                "SELECT column_name, data_type, is_nullable FROM information_schema.columns "
                        + "WHERE table_schema = ? AND table_name = ? ORDER BY ordinal_position")) {
            query.setString(1, schema);
            query.setString(2, table);
            try (ResultSet result = query.executeQuery()) {
                for (String[] column : expected) {
                    assertThat(result.next()).isTrue();
                    assertThat(result.getString("column_name")).isEqualTo(column[0]);
                    assertThat(result.getString("data_type")).isEqualTo(column[1]);
                    assertThat(result.getString("is_nullable")).isEqualTo(column[2]);
                }
                assertThat(result.next()).isFalse();
            }
        }
    }

    private static void assertCheckConstraint(
            Connection connection,
            String schema,
            String table,
            String... expectedFragments
    )
            throws SQLException {
        try (PreparedStatement query = connection.prepareStatement(
                "SELECT pg_get_constraintdef(c.oid) FROM pg_constraint c "
                        + "JOIN pg_class t ON t.oid = c.conrelid "
                        + "JOIN pg_namespace n ON n.oid = t.relnamespace "
                        + "WHERE n.nspname = ? AND t.relname = ? AND c.contype = 'c'")) {
            query.setString(1, schema);
            query.setString(2, table);
            try (ResultSet result = query.executeQuery()) {
                while (result.next()) {
                    String definition = normalizeSql(result.getString(1));
                    boolean matches = true;
                    for (String fragment : expectedFragments) {
                        matches &= definition.contains(normalizeSql(fragment));
                    }
                    if (matches) {
                        return;
                    }
                }
                throw new AssertionError("Missing CHECK constraint semantics for "
                        + schema + "." + table + ": " + String.join(", ", expectedFragments));
            }
        }
    }

    private static void assertPrimaryKey(Connection connection, String schema, String table, String... columns)
            throws SQLException {
        assertConstraintColumns(connection, schema, table, "PRIMARY KEY", columns);
    }

    private static void assertUniqueConstraint(Connection connection, String schema, String table, String... columns)
            throws SQLException {
        assertConstraintColumns(connection, schema, table, "UNIQUE", columns);
    }

    private static void assertConstraintColumns(
            Connection connection,
            String schema,
            String table,
            String constraintType,
            String... expectedColumns
    ) throws SQLException {
        try (PreparedStatement query = connection.prepareStatement(
                "SELECT k.column_name FROM information_schema.key_column_usage k "
                        + "JOIN information_schema.table_constraints t ON t.constraint_name = k.constraint_name "
                        + "AND t.constraint_schema = k.constraint_schema "
                        + "WHERE k.table_schema = ? AND k.table_name = ? AND t.constraint_type = ? "
                        + "ORDER BY k.ordinal_position")) {
            query.setString(1, schema);
            query.setString(2, table);
            query.setString(3, constraintType);
            try (ResultSet result = query.executeQuery()) {
                Set<String> actualColumns = new java.util.LinkedHashSet<>();
                while (result.next()) {
                    actualColumns.add(result.getString(1));
                }
                assertThat(actualColumns).containsExactly(expectedColumns);
            }
        }
    }

    private static int columnLength(Connection connection, String schema, String table, String column)
            throws SQLException {
        try (PreparedStatement query = connection.prepareStatement(
                "SELECT character_maximum_length FROM information_schema.columns "
                        + "WHERE table_schema = ? AND table_name = ? AND column_name = ?")) {
            query.setString(1, schema);
            query.setString(2, table);
            query.setString(3, column);
            try (ResultSet result = query.executeQuery()) {
                assertThat(result.next()).isTrue();
                return result.getInt(1);
            }
        }
    }

    private static String columnDefault(Connection connection, String schema, String table, String column)
            throws SQLException {
        try (PreparedStatement query = connection.prepareStatement(
                "SELECT column_default FROM information_schema.columns "
                        + "WHERE table_schema = ? AND table_name = ? AND column_name = ?")) {
            query.setString(1, schema);
            query.setString(2, table);
            query.setString(3, column);
            try (ResultSet result = query.executeQuery()) {
                assertThat(result.next()).isTrue();
                return result.getString(1);
            }
        }
    }

    private static void assertIndex(
            Connection connection,
            String schema,
            String table,
            String index,
            String[] expectedColumns,
            String expectedPredicate
    ) throws SQLException {
        try (PreparedStatement query = connection.prepareStatement(
                "SELECT array_agg(a.attname ORDER BY keys.ordinality), "
                        + "pg_get_expr(i.indpred, i.indrelid) "
                        + "FROM pg_index i "
                        + "JOIN pg_class index_class ON index_class.oid = i.indexrelid "
                        + "JOIN pg_class table_class ON table_class.oid = i.indrelid "
                        + "JOIN pg_namespace n ON n.oid = table_class.relnamespace "
                        + "CROSS JOIN LATERAL unnest(i.indkey) WITH ORDINALITY keys(attnum, ordinality) "
                        + "JOIN pg_attribute a ON a.attrelid = i.indrelid AND a.attnum = keys.attnum "
                        + "WHERE n.nspname = ? AND table_class.relname = ? AND index_class.relname = ? "
                        + "GROUP BY i.indpred, i.indrelid")) {
            query.setString(1, schema);
            query.setString(2, table);
            query.setString(3, index);
            try (ResultSet result = query.executeQuery()) {
                assertThat(result.next()).isTrue();
                assertThat((String[]) result.getArray(1).getArray()).containsExactly(expectedColumns);
                String actualPredicate = result.getString(2);
                if (expectedPredicate == null) {
                    assertThat(actualPredicate).isNull();
                } else {
                    assertThat(normalizeSql(actualPredicate)).isEqualTo(normalizeSql(expectedPredicate));
                }
                assertThat(result.next()).isFalse();
            }
        }
    }

    private static String normalizeSql(String sql) {
        return sql.toLowerCase().replaceAll("[\\s()]", "");
    }

    private static boolean foreignKeyExists(Connection connection, String schema, String table, String column)
            throws SQLException {
        try (PreparedStatement query = connection.prepareStatement(
                "SELECT 1 FROM information_schema.key_column_usage k "
                        + "JOIN information_schema.table_constraints t ON t.constraint_name = k.constraint_name "
                        + "AND t.constraint_schema = k.constraint_schema WHERE k.table_schema = ? AND k.table_name = ? "
                        + "AND k.column_name = ? AND t.constraint_type = 'FOREIGN KEY'")) {
            query.setString(1, schema);
            query.setString(2, table);
            query.setString(3, column);
            try (ResultSet result = query.executeQuery()) {
                return result.next();
            }
        }
    }

    private static Connection connection() throws SQLException {
        return java.sql.DriverManager.getConnection(
                postgres.getJdbcUrl(), postgres.getUsername(), postgres.getPassword());
    }
}
