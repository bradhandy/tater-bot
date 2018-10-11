package net.jackofalltrades.taterbot.channel.record;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;

@ExtendWith(MockitoExtension.class)
class ChannelRecordRowMapperTest {

    @Mock
    private ResultSet resultSet;

    private ChannelRecordRowMapper channelRecordRowMapper;

    @BeforeEach
    void setUpChannelRecordRowMapper() {
        channelRecordRowMapper = new ChannelRecordRowMapper();
    }

    @Test
    void createChannelRecordWithUserInformation() throws SQLException {
        LocalDateTime messageTimestamp = LocalDateTime.now();

        doReturn("channelId").when(resultSet).getString("channel_id");
        doReturn("userId").when(resultSet).getString("user_id");
        doReturn("displayName").when(resultSet).getString("user_display_name");
        doReturn("text").when(resultSet).getString("message_type");
        doReturn(Timestamp.valueOf(messageTimestamp)).when(resultSet).getTimestamp("message_timestamp");
        doReturn("message").when(resultSet).getString("message");

        ChannelRecord channelRecord = channelRecordRowMapper.mapRow(resultSet, 1);

        assertEquals(channelRecord,
                new ChannelRecord("channelId", "userId", "displayName", "text", messageTimestamp, "message"),
                "The channel record does not match.");
    }

    @Test
    void createChannelRecordWithoutUserInformation() throws SQLException {
        LocalDateTime messageTimestamp = LocalDateTime.now();

        doReturn("channelId").when(resultSet).getString("channel_id");
        doReturn(null).when(resultSet).getString("user_id");
        doReturn(null).when(resultSet).getString("user_display_name");
        doReturn("text").when(resultSet).getString("message_type");
        doReturn(Timestamp.valueOf(messageTimestamp)).when(resultSet).getTimestamp("message_timestamp");
        doReturn("message").when(resultSet).getString("message");

        ChannelRecord channelRecord = channelRecordRowMapper.mapRow(resultSet, 1);

        assertEquals(channelRecord,
                new ChannelRecord("channelId", null, null, "text", messageTimestamp, "message"),
                "The channel record does not match.");

        verify(resultSet, times(1)).getString("user_id");
        verify(resultSet, times(1)).getString("user_display_name");
    }

}
