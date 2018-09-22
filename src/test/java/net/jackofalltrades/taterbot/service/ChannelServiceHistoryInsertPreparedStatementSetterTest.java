package net.jackofalltrades.taterbot.service;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@ExtendWith(MockitoExtension.class)
class ChannelServiceHistoryInsertPreparedStatementSetterTest {

    @Mock
    private PreparedStatement preparedStatement;

    @Test
    void fieldsSetCorrectly() throws SQLException {
        LocalDateTime beginDate = LocalDateTime.now().minus(5, ChronoUnit.DAYS);
        LocalDateTime endDate = LocalDateTime.now();
        ChannelServiceHistory channelServiceHistory = new ChannelServiceHistory("channelId", "service",
                Service.Status.ACTIVE, beginDate, endDate, "userId");

        ChannelServiceHistoryInsertPreparedStatementSetter channelServiceHistoryInsertPreparedStatementSetter =
                new ChannelServiceHistoryInsertPreparedStatementSetter(channelServiceHistory);

        channelServiceHistoryInsertPreparedStatementSetter.setValues(preparedStatement);

        verify(preparedStatement, times(1)).setString(1, "channelId");
        verify(preparedStatement, times(1)).setString(2, "service");
        verify(preparedStatement, times(1)).setString(3, "active");
        verify(preparedStatement, times(1)).setTimestamp(4, Timestamp.valueOf(beginDate));
        verify(preparedStatement, times(1)).setTimestamp(5, Timestamp.valueOf(endDate));
        verify(preparedStatement, times(1)).setString(6, "userId");

        assertSame(channelServiceHistory, channelServiceHistoryInsertPreparedStatementSetter.getChannelServiceHistory(),
                "The channel service history does not match.");
    }

    @Test
    void fieldsSetCorrectlyWhenUserIdIsNull() throws SQLException {
        LocalDateTime beginDate = LocalDateTime.now().minus(5, ChronoUnit.DAYS);
        LocalDateTime endDate = LocalDateTime.now();
        ChannelServiceHistory channelServiceHistory = new ChannelServiceHistory("channelId", "service",
                Service.Status.ACTIVE, beginDate, endDate, null);

        ChannelServiceHistoryInsertPreparedStatementSetter channelServiceHistoryInsertPreparedStatementSetter =
                new ChannelServiceHistoryInsertPreparedStatementSetter(channelServiceHistory);

        channelServiceHistoryInsertPreparedStatementSetter.setValues(preparedStatement);

        verify(preparedStatement, times(1)).setString(1, "channelId");
        verify(preparedStatement, times(1)).setString(2, "service");
        verify(preparedStatement, times(1)).setString(3, "active");
        verify(preparedStatement, times(1)).setTimestamp(4, Timestamp.valueOf(beginDate));
        verify(preparedStatement, times(1)).setTimestamp(5, Timestamp.valueOf(endDate));
        verify(preparedStatement, times(1)).setNull(6, Types.VARCHAR);
    }

    @Test
    void fieldsSetCorrectlyWhenUserIdIsEmpty() throws SQLException {
        LocalDateTime beginDate = LocalDateTime.now().minus(5, ChronoUnit.DAYS);
        LocalDateTime endDate = LocalDateTime.now();
        ChannelServiceHistory channelServiceHistory = new ChannelServiceHistory("channelId", "service",
                Service.Status.ACTIVE, beginDate, endDate, "");

        ChannelServiceHistoryInsertPreparedStatementSetter channelServiceHistoryInsertPreparedStatementSetter =
                new ChannelServiceHistoryInsertPreparedStatementSetter(channelServiceHistory);

        channelServiceHistoryInsertPreparedStatementSetter.setValues(preparedStatement);

        verify(preparedStatement, times(1)).setString(1, "channelId");
        verify(preparedStatement, times(1)).setString(2, "service");
        verify(preparedStatement, times(1)).setString(3, "active");
        verify(preparedStatement, times(1)).setTimestamp(4, Timestamp.valueOf(beginDate));
        verify(preparedStatement, times(1)).setTimestamp(5, Timestamp.valueOf(endDate));
        verify(preparedStatement, times(1)).setNull(6, Types.VARCHAR);
    }

}