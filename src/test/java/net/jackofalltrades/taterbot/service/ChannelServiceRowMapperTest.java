package net.jackofalltrades.taterbot.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.doReturn;

import org.junit.jupiter.api.BeforeEach;
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
class ChannelServiceRowMapperTest {

    @Mock
    private ResultSet resultSet;

    private ChannelServiceRowMapper channelServiceRowMapper;

    @BeforeEach
    void setUpRowMapper() {
        channelServiceRowMapper = new ChannelServiceRowMapper();
    }

    @Test
    void channelServiceRecordRowMapsSuccessfully() throws SQLException {
        LocalDateTime channelServiceStatusDate = LocalDateTime.now().minus(5, ChronoUnit.DAYS);
        setUpMockChannelServiceTableData("channelId", "code", "active", channelServiceStatusDate, "userId");

        ChannelService channelService = channelServiceRowMapper.mapRow(resultSet, 1);

        assertEquals("channelId", channelService.getChannelId(), "The channel id does not match.");
        assertEquals("code", channelService.getServiceCode(), "The service did not match.");
        assertEquals(Service.Status.ACTIVE, channelService.getStatus(), "The channel service status did not match.");
        assertEquals(channelServiceStatusDate, channelService.getStatusDate(), "The status date does not match.");
        assertEquals("userId", channelService.getUserId(), "The user id does not match.");
    }

    @Test
    void channelServiceRecordMapsWhenStatusIsUnknownAndStatusDateIsNull() throws SQLException {
        LocalDateTime channelServiceStatusDate = LocalDateTime.now();
        setUpMockChannelServiceTableData("channelId", "code", "non-existent", null, "userId");

        ChannelService channelService = channelServiceRowMapper.mapRow(resultSet, 1);

        assertEquals("channelId", channelService.getChannelId(), "The channel id does not match.");
        assertEquals("code", channelService.getServiceCode(), "The service did not match.");
        assertEquals(Service.Status.DISABLED, channelService.getStatus(), "The channel service status did not match.");
        assertFalse(channelServiceStatusDate.isAfter(channelService.getStatusDate()),
                "The status date should be on or after the time the test started.");
        assertEquals("userId", channelService.getUserId(), "The user id does not match.");
    }

    private void setUpMockChannelServiceTableData(String channelId, String code, String status, LocalDateTime statusDate,
            String userId) throws SQLException {
        doReturn(channelId).when(resultSet).getString("channel_id");
        doReturn(code).when(resultSet).getString("service_code");
        doReturn(status).when(resultSet).getString("status");
        doReturn(statusDate == null ? null : Timestamp.valueOf(statusDate)).when(resultSet).getTimestamp("status_date");
        if (statusDate == null) {
            doReturn(true, false).when(resultSet).wasNull();
        }
        doReturn(userId).when(resultSet).getString("user_id");
    }

}
