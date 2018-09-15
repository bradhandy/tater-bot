package net.jackofalltrades.taterbot.service;

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

@ExtendWith(MockitoExtension.class)
class ChannelServiceInsertPreparedStatementSetterTest {

    @Mock
    private PreparedStatement preparedStatement;

    @Test
    void fieldsSetCorrectly() throws SQLException {
        LocalDateTime currentDateTime = LocalDateTime.now();
        ChannelService channelService = new ChannelService("channelId", "service", Service.Status.ACTIVE,
                currentDateTime, "user_id");

        ChannelServiceInsertPreparedStatementSetter channelServiceInsertPreparedStatementSetter =
                new ChannelServiceInsertPreparedStatementSetter(channelService);

        channelServiceInsertPreparedStatementSetter.setValues(preparedStatement);

        verify(preparedStatement, times(1)).setString(1, "channelId");
        verify(preparedStatement, times(1)).setString(2, "service");
        verify(preparedStatement, times(1)).setString(3, "active");
        verify(preparedStatement, times(1)).setTimestamp(4, Timestamp.valueOf(currentDateTime));
        verify(preparedStatement, times(1)).setString(5, "user_id");
    }

    @Test
    void fieldsSetCorrectlyWhenUserIdIsNull() throws SQLException {
        LocalDateTime currentDateTime = LocalDateTime.now();
        ChannelService channelService = new ChannelService("channelId", "service", Service.Status.ACTIVE,
                currentDateTime, null);

        ChannelServiceInsertPreparedStatementSetter channelServiceInsertPreparedStatementSetter =
                new ChannelServiceInsertPreparedStatementSetter(channelService);

        channelServiceInsertPreparedStatementSetter.setValues(preparedStatement);

        verify(preparedStatement, times(1)).setString(1, "channelId");
        verify(preparedStatement, times(1)).setString(2, "service");
        verify(preparedStatement, times(1)).setString(3, "active");
        verify(preparedStatement, times(1)).setTimestamp(4, Timestamp.valueOf(currentDateTime));
        verify(preparedStatement, times(1)).setNull(5, Types.VARCHAR);
    }

    @Test
    void fieldsSetCorrectlyWhenUserIdIsEmpty() throws SQLException {
        LocalDateTime currentDateTime = LocalDateTime.now();
        ChannelService channelService = new ChannelService("channelId", "service", Service.Status.ACTIVE,
                currentDateTime, "");

        ChannelServiceInsertPreparedStatementSetter channelServiceInsertPreparedStatementSetter =
                new ChannelServiceInsertPreparedStatementSetter(channelService);

        channelServiceInsertPreparedStatementSetter.setValues(preparedStatement);

        verify(preparedStatement, times(1)).setString(1, "channelId");
        verify(preparedStatement, times(1)).setString(2, "service");
        verify(preparedStatement, times(1)).setString(3, "active");
        verify(preparedStatement, times(1)).setTimestamp(4, Timestamp.valueOf(currentDateTime));
        verify(preparedStatement, times(1)).setNull(5, Types.VARCHAR);
    }

}