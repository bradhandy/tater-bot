package net.jackofalltrades.taterbot.channel;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
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
class ChannelHistoryRowMapperTest {

    @Mock
    private ResultSet resultSet;

    @Test
    void createChannelHistoryRecordWhenJoined() throws SQLException {
        LocalDateTime beginDate = LocalDateTime.now().minus(5, ChronoUnit.MINUTES);
        LocalDateTime endDate = LocalDateTime.now();
        ChannelHistoryRowMapper channelHistoryRowMapper = new ChannelHistoryRowMapper();

        doReturn("channelId").when(resultSet).getString("channel_id");
        doReturn("Y").when(resultSet).getString("member");
        doReturn("Invited").when(resultSet).getString("member_reason");
        doReturn(Timestamp.valueOf(beginDate)).when(resultSet).getTimestamp("begin_date");
        doReturn(Timestamp.valueOf(endDate)).when(resultSet).getTimestamp("end_date");

        ChannelHistory channelHistory = channelHistoryRowMapper.mapRow(resultSet, 1);

        assertEquals("channelId", channelHistory.getChannelId(), "The channel id does not match.");
        assertTrue(channelHistory.isMember(), "The membership flag does not match.");
        assertEquals("Invited", channelHistory.getMemberReason(), "The membership reason does not match.");
        assertEquals(beginDate, channelHistory.getBeginDate(), "The begin date does not match.");
        assertEquals(endDate, channelHistory.getEndDate(), "The end date does not match.");
    }

    @Test
    void createChannelHistoryRecordWhenLeft() throws SQLException {
        LocalDateTime beginDate = LocalDateTime.now().minus(5, ChronoUnit.MINUTES);
        LocalDateTime endDate = LocalDateTime.now();
        ChannelHistoryRowMapper channelHistoryRowMapper = new ChannelHistoryRowMapper();

        doReturn("channelId").when(resultSet).getString("channel_id");
        doReturn("N").when(resultSet).getString("member");
        doReturn("Kicked").when(resultSet).getString("member_reason");
        doReturn(Timestamp.valueOf(beginDate)).when(resultSet).getTimestamp("begin_date");
        doReturn(Timestamp.valueOf(endDate)).when(resultSet).getTimestamp("end_date");

        ChannelHistory channelHistory = channelHistoryRowMapper.mapRow(resultSet, 1);

        assertEquals("channelId", channelHistory.getChannelId(), "The channel id does not match.");
        assertFalse(channelHistory.isMember(), "The membership flag does not match.");
        assertEquals("Kicked", channelHistory.getMemberReason(), "The membership reason does not match.");
        assertEquals(beginDate, channelHistory.getBeginDate(), "The begin date does not match.");
        assertEquals(endDate, channelHistory.getEndDate(), "The end date does not match.");
    }

}