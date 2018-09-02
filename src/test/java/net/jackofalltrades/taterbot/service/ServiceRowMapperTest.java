package net.jackofalltrades.taterbot.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.doReturn;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@ExtendWith(MockitoExtension.class)
class ServiceRowMapperTest {

    @Mock
    private ResultSet resultSet;

    @Test
    void serviceRecordRowMapsSuccessfully() throws SQLException {
        LocalDateTime statusDate = LocalDateTime.now().minus(1, ChronoUnit.DAYS);

        setUpMockServiceTableData("code", "description", "enabled", statusDate, "inactive");

        ServiceRowMapper serviceRowMapper = new ServiceRowMapper();
        Service service = serviceRowMapper.mapRow(resultSet, 1);

        assertNotNull(service, "There should have been a Service object returned.");
        assertEquals("code", service.getCode(), "The service code did not match.");
        assertEquals("description", service.getDescription(), "The service description did not match.");
        assertEquals(Service.Status.ENABLED, service.getStatus(), "The service status did not match.");
        assertEquals(statusDate, service.getStatusDate(), "The service status date did not match.");
        assertEquals(Service.Status.INACTIVE, service.getInitialChannelStatus(),
                "The initial channel status did not match.");
    }

    @Test
    void serviceRowStatusesDefaultToDisabledWhenInvalidOrNull() throws SQLException {
        LocalDateTime statusDate = LocalDateTime.now().minus(1, ChronoUnit.DAYS);

        setUpMockServiceTableData("code", "description", "invalid", statusDate, null);

        ServiceRowMapper serviceRowMapper = new ServiceRowMapper();
        Service service = serviceRowMapper.mapRow(resultSet, 1);

        assertNotNull(service, "There should have been a Service object returned.");
        assertEquals("code", service.getCode(), "The service code did not match.");
        assertEquals("description", service.getDescription(), "The service description did not match.");
        assertEquals(Service.Status.DISABLED, service.getStatus(), "The service status did not match.");
        assertEquals(statusDate, service.getStatusDate(), "The service status date did not match.");
        assertEquals(Service.Status.DISABLED, service.getInitialChannelStatus(),
                "The initial channel status did not match.");
    }

    @Test
    void serviceRowStatusDateDefaultsToDateOfTodayWhenMissing() throws SQLException {
        LocalDateTime lowerBoundsStatusDate = LocalDateTime.now();

        setUpMockServiceTableData("code", "description", "invalid", null, "active");

        ServiceRowMapper serviceRowMapper = new ServiceRowMapper();
        Service service = serviceRowMapper.mapRow(resultSet, 1);

        assertNotNull(service, "There should have been a Service object returned.");
        assertEquals("code", service.getCode(), "The service code did not match.");
        assertEquals("description", service.getDescription(), "The service description did not match.");
        assertEquals(Service.Status.DISABLED, service.getStatus(), "The service status did not match.");
        assertFalse(service.getStatusDate().isBefore(lowerBoundsStatusDate),
                "The service status date should have defaulted to the current time.");
        assertEquals(Service.Status.ACTIVE, service.getInitialChannelStatus(),
                "The initial channel status did not match.");
    }

    private void setUpMockServiceTableData(String code, String description, String status,
            LocalDateTime statusDate, String initialChannelStatus) throws SQLException {
        doReturn(code).when(resultSet).getString("code");
        doReturn(description).when(resultSet).getString("description");
        doReturn(status).when(resultSet).getString("status");
        doReturn(statusDate == null ? null : Timestamp.valueOf(statusDate)).when(resultSet).getTimestamp("status_date");
        if (statusDate == null) {
            doReturn(true, false).when(resultSet).wasNull();
        }
        doReturn(initialChannelStatus).when(resultSet).getString("initial_channel_status");
    }

}
