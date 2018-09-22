package net.jackofalltrades.taterbot.channel;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@ExtendWith(MockitoExtension.class)
class ChannelUpdatePreparedStatementSetterTest {

    @Mock
    private PreparedStatement preparedStatement;

    @Test
    void fieldsSetCorrectlyWhenLeavingChannel() throws SQLException {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime originalDate = now.minus(5, ChronoUnit.MINUTES);
        Channel channel = new Channel("channelId", true, "Invited", originalDate);

        ChannelUpdatePreparedStatementSetter channelUpdatePreparedStatementSetter =
                new ChannelUpdatePreparedStatementSetter(channel, false, "Kicked", now);

        channelUpdatePreparedStatementSetter.setValues(preparedStatement);

        verify(preparedStatement, times(1)).setString(1, "N");
        verify(preparedStatement, times(1)).setString(2, "Kicked");
        verify(preparedStatement, times(1)).setTimestamp(3, Timestamp.valueOf(now));
        verify(preparedStatement, times(1)).setString(4, "channelId");
        verify(preparedStatement, times(1)).setString(5, "Y");
        verify(preparedStatement, times(1)).setString(6, "Invited");
        verify(preparedStatement, times(1)).setTimestamp(7, Timestamp.valueOf(originalDate));
    }

    @Test
    void fieldsSetCorrectlyWhenJoiningChannel() throws SQLException {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime originalDate = now.minus(5, ChronoUnit.MINUTES);
        Channel channel = new Channel("channelId", false, "Kicked", originalDate);

        ChannelUpdatePreparedStatementSetter channelUpdatePreparedStatementSetter =
                new ChannelUpdatePreparedStatementSetter(channel, true, "Invited", now);

        channelUpdatePreparedStatementSetter.setValues(preparedStatement);

        verify(preparedStatement, times(1)).setString(1, "Y");
        verify(preparedStatement, times(1)).setString(2, "Invited");
        verify(preparedStatement, times(1)).setTimestamp(3, Timestamp.valueOf(now));
        verify(preparedStatement, times(1)).setString(4, "channelId");
        verify(preparedStatement, times(1)).setString(5, "N");
        verify(preparedStatement, times(1)).setString(6, "Kicked");
        verify(preparedStatement, times(1)).setTimestamp(7, Timestamp.valueOf(originalDate));
    }

}