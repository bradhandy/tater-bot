package net.jackofalltrades.taterbot;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import com.google.common.collect.ImmutableMap;
import net.jackofalltrades.taterbot.service.Service;
import net.jackofalltrades.taterbot.service.ServiceManager;
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
class InitialDatabaseMigrationTest {

    private static final Map<String, String> EXPECTED_TABLES_IN_SCHEMAS =
            ImmutableMap.<String, String>builder().put("SERVICE", "PUBLIC").put("SERVICE_HISTORY", "PUBLIC")
                    .put("CHANNEL_SERVICE", "PUBLIC").put("CHANNEL_SERVICE_HISTORY", "PUBLIC")
                    .put("DATABASECHANGELOG", "PUBLIC").put("DATABASECHANGELOGLOCK", "PUBLIC").build();

    @Autowired private JdbcTemplate testDatabaseTemplate;

    @Autowired private ServiceManager serviceManager;

    @Test
    void databaseTablesCreatedSuccessfully() {
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
    void serviceTableFunctionalityWorksCorrectly() {
        recordServiceCreatedSuccessfully();
        updatingRecordStatusCreatesHistoryRecord();
    }

    @Test
    void missingServiceReturnsUnknownService() {
        assertSame("The service does not match.", Service.UNKNOWN_SERVICE,
                serviceManager.findServiceByCode("notthere"));
    }

    private void recordServiceCreatedSuccessfully() {
        LocalDateTime expectedStatusDate = LocalDateTime.now().minus(60, ChronoUnit.SECONDS);
        Service recordingService = serviceManager.findServiceByCode(Service.RECORD_SERVICE_CODE);

        assertEquals("The service code did not match.", Service.RECORD_SERVICE_CODE, recordingService.getCode());
        assertEquals("The service description did not match.",
                "Keep a log of channel conversation for a period of time.", recordingService.getDescription());
        assertEquals("The status does not match.", Service.Status.ACTIVE, recordingService.getStatus());
        assertTrue("The status date/time does not meet expectations.",
                recordingService.getStatusDate().isAfter(expectedStatusDate));
        assertEquals("The initial channel service does not match.", Service.Status.INACTIVE,
                recordingService.getInitialChannelStatus());
    }

    private void updatingRecordStatusCreatesHistoryRecord() {
        Service recordingService = serviceManager.findServiceByCode(Service.RECORD_SERVICE_CODE);
        serviceManager.updateServiceStatus(recordingService, Service.Status.DISABLED);

        Service updatedService = serviceManager.findServiceByCode(Service.RECORD_SERVICE_CODE);

        assertEquals("The status does not match.", Service.Status.DISABLED, updatedService.getStatus());
        assertTrue("The update service date should be after the original date.",
                recordingService.getStatusDate().isBefore(updatedService.getStatusDate()));

        @SuppressWarnings("ConstantConditions")
        int numberOfHistoryRecords =
                testDatabaseTemplate.queryForObject("select count(*) from service_history where code = ?",
                        Integer.class, Service.RECORD_SERVICE_CODE);
        assertEquals("The number of history records does not match.", 1, numberOfHistoryRecords);

        testDatabaseTemplate.query("select * from service_history where code = ?",
                resultSet -> {
                    LocalDateTime historyBeginDate = resultSet.getTimestamp("begin_date").toLocalDateTime();
                    assertEquals("The history begin date should be the status date of the original record.",
                            recordingService.getStatusDate(), historyBeginDate);

                    LocalDateTime historyEndDate = resultSet.getTimestamp("end_date").toLocalDateTime();
                    assertEquals("The history end date should be the status date of the new record.",
                            updatedService.getStatusDate(), historyEndDate);
                },
                Service.RECORD_SERVICE_CODE);
    }

    @Configuration
    @ComponentScan(basePackages = "net.jackofalltrades.taterbot.service")
    @EnableAutoConfiguration
    static class InitialDatabaseMigrationTestsConfiguration {

    }

}
