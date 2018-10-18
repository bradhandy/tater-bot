package net.jackofalltrades.taterbot;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import net.jackofalltrades.taterbot.service.ChannelService;
import net.jackofalltrades.taterbot.service.ChannelServiceFactory;
import net.jackofalltrades.taterbot.service.ChannelServiceKey;
import net.jackofalltrades.taterbot.service.ChannelServiceManager;
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
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Map;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = InitialDatabaseMigrationTest.InitialDatabaseMigrationTestsConfiguration.class, webEnvironment = SpringBootTest.WebEnvironment.NONE)
@TestPropertySource(locations = "initial-db-migration-tests.properties")
public class InitialDatabaseMigrationTest {

    private static final Map<String, String> EXPECTED_TABLES_IN_SCHEMAS =
            ImmutableMap.<String, String>builder().put("SERVICE", "PUBLIC")
                    .put("SERVICE_HISTORY", "PUBLIC")
                    .put("CHANNEL_SERVICE", "PUBLIC")
                    .put("CHANNEL_SERVICE_HISTORY", "PUBLIC")
                    .put("CHANNEL", "PUBLIC")
                    .put("CHANNEL_HISTORY", "PUBLIC")
                    .put("ADMIN", "PUBLIC")
                    .put("ADMIN_HISTORY", "PUBLIC")
                    .put("CHANNEL_ADMIN", "PUBLIC")
                    .put("CHANNEL_ADMIN_HISTORY", "PUBLIC")
                    .put("CHANNEL_RECORD", "PUBLIC")
                    .put("DATABASECHANGELOG", "PUBLIC")
                    .put("DATABASECHANGELOGLOCK", "PUBLIC").build();

    @Autowired
    private JdbcTemplate testDatabaseTemplate;

    @Autowired
    private ServiceManager serviceManager;

    @Autowired
    private ChannelServiceManager channelServceManager;

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
    public void serviceTableFunctionalityWorksCorrectly() {
        recordServiceCreatedSuccessfully();
        updatingRecordStatusCreatesHistoryRecord();
    }

    private void recordServiceCreatedSuccessfully() {
        LocalDateTime expectedStatusDate = LocalDateTime.now().minus(60, ChronoUnit.SECONDS);
        Service recordingService = serviceManager.findServiceByCode(Service.RECORD_SERVICE_CODE);

        assertEquals("The service code did not match.", Service.RECORD_SERVICE_CODE, recordingService.getCode());
        assertEquals("The service description did not match.", "Channel Recording", recordingService.getDescription());
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

    @Test
    public void missingServiceReturnsUnknownService() {
        assertSame("The service does not match.", Service.UNKNOWN_SERVICE,
                serviceManager.findServiceByCode("notthere"));
    }

    @Test
    public void channelServiceFunctionalityWorksCorrectly() {
        addMissingServicesToChannel();
        updateRecordStatusToDisabled();
    }

    private void addMissingServicesToChannel() {
        ChannelService recordChannelService = ChannelServiceFactory.createChannelServiceFactory("channelId", "record",
                Service.Status.INACTIVE, LocalDateTime.now(), null);
        Map<String, ChannelService> expectedChannelServicesMap = Maps.newHashMap();
        expectedChannelServicesMap.put("record", recordChannelService);

        assertFalse("The channel service should not be present.",
                channelServceManager.findChannelServiceByKey(new ChannelServiceKey("channelId", "record")).isPresent());

        channelServceManager.addMissingServicesToChannel("channelId");

        testDatabaseTemplate.query("select * from channel_service where channel_id = ?",
                preparedStatement -> {
                    preparedStatement.setString(1, "channelId");
                },
                resultSet -> {
                    ChannelService channelService = expectedChannelServicesMap.remove(resultSet.getString("service_code"));
                    if (channelService != null) {
                        assertEquals("The channel id does not match.",
                                channelService.getChannelId(), resultSet.getString("channel_id"));
                        assertEquals("The service code does not match.",
                                channelService.getServiceCode(), resultSet.getString("service_code"));
                        assertEquals("The status does not match.", "inactive", resultSet.getString("status"));
                    }
                });
        assertTrue("Missing validation of some required services.", expectedChannelServicesMap.isEmpty());
    }

    private void updateRecordStatusToDisabled() {
        ChannelServiceKey channelServiceKey = new ChannelServiceKey("channelId", "record");
        Optional<ChannelService> channelService = channelServceManager.findChannelServiceByKey(channelServiceKey);
        assertTrue("There should have been a record service for channelId.", channelService.isPresent());

        channelServceManager.updateChannelServiceStatus(channelService.get(), Service.Status.DISABLED, "bradhandy");

        Optional<ChannelService> updatedChannelService =
                channelServceManager.findChannelServiceByKey(channelServiceKey);
        assertTrue("There should have been a record service updated for channelId.", updatedChannelService.isPresent());

        assertEquals("The status should have been disabled.", Service.Status.DISABLED,
                updatedChannelService.get().getStatus());
        assertTrue("The original status date should be on or before the updated status date.",
                !channelService.get().getStatusDate().isAfter(updatedChannelService.get().getStatusDate()));

        @SuppressWarnings("ConstantConditions")
        int numberOfHistoryRecords = testDatabaseTemplate.query(
                "select count(*) numHistory from channel_service_history where channel_id = ? and service_code = ? " +
                        "and status = ? and begin_date = ? and end_date = ?",
                preparedStatement -> {
                        preparedStatement.setString(1, "channelId");
                        preparedStatement.setString(2, "record");
                        preparedStatement.setString(3, "inactive");
                        preparedStatement.setTimestamp(4, Timestamp.valueOf(channelService.get().getStatusDate()));
                        preparedStatement.setTimestamp(5, Timestamp.valueOf(updatedChannelService.get().getStatusDate()));
                    },
                resultSet -> resultSet.next() ? resultSet.getInt("numHistory") : 0);
        assertEquals("There should be one history record.", 1, numberOfHistoryRecords);
    }

    @Configuration
    @ComponentScan(basePackages = "net.jackofalltrades.taterbot.service")
    @EnableAutoConfiguration
    static class InitialDatabaseMigrationTestsConfiguration {

    }

}
