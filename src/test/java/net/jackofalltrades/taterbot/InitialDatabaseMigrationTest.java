package net.jackofalltrades.taterbot;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.google.common.cache.LoadingCache;
import com.google.common.collect.ImmutableMap;
import net.jackofalltrades.taterbot.service.Service;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Map;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = InitialDatabaseMigrationTest.InitialDatabaseMigrationTestsConfiguration.class, webEnvironment = SpringBootTest.WebEnvironment.NONE)
@TestPropertySource(locations = "initial-db-migration-tests.properties")
public class InitialDatabaseMigrationTest {

    private static final Map<String, String> EXPECTED_TABLES_IN_SCHEMAS =
            ImmutableMap.<String, String>builder()
                    .put("SERVICE", "PUBLIC")
                    .put("SERVICE_HISTORY", "PUBLIC")
                    .put("CHANNEL_SERVICE", "PUBLIC")
                    .put("CHANNEL_SERVICE_HISTORY", "PUBLIC")
                    .put("DATABASECHANGELOG", "PUBLIC")
                    .put("DATABASECHANGELOGLOCK", "PUBLIC")
                    .build();

    @Autowired
    private JdbcTemplate testDatabaseTemplate;

    @Autowired
    private LoadingCache<String, Service> serviceLoadingCache;

    @Test
    public void databaseTablesCreatedSuccessfully() {
        Map<String, String> actualTableToSchemaMap =
                testDatabaseTemplate.query("show tables from public", (ResultSetExtractor<Map<String, String>>) rs -> {
                    ImmutableMap.Builder<String, String> tableToSchemaMap = ImmutableMap.builder();
                    while (rs.next()) {
                        tableToSchemaMap.put(rs.getString("TABLE_NAME"), rs.getString("TABLE_SCHEMA"));
                    }

                    return tableToSchemaMap.build();
                });

        assertEquals("The tables created did not match the expected tables.", EXPECTED_TABLES_IN_SCHEMAS,
                actualTableToSchemaMap);
    }

    @Test
    public void recordServiceCreatedSuccessfully() {
        LocalDateTime expectedStatusDate = LocalDateTime.now().minus(60, ChronoUnit.SECONDS);
        Service recordingService = serviceLoadingCache.getUnchecked("record");

        assertEquals("The service code did not match.", "record", recordingService.getCode());
        assertEquals("The service description did not match.",
                "Keep a log of channel conversation for a period of time.", recordingService.getDescription());
        assertEquals("The status does not match.", Service.Status.ENABLED, recordingService.getStatus());
        assertTrue("The status date/time does not meet expectations.",
                recordingService.getStatusDate().isAfter(expectedStatusDate));
        assertEquals("The initial channel service does not match.", Service.Status.INACTIVE,
                recordingService.getInitialChannelStatus());
    }

    @Configuration
    @ComponentScan(basePackages = "net.jackofalltrades.taterbot.service")
    @EnableAutoConfiguration
    static class InitialDatabaseMigrationTestsConfiguration {

    }

}
