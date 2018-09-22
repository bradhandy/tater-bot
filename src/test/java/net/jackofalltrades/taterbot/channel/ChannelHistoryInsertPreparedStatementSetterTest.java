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
class ChannelHistoryInsertPreparedStatementSetterTest {

    @Mock
    private PreparedStatement preparedStatement;

    @Test
    void fieldsAreSetCorrectlyWhenMemberIsTrue() throws SQLException {
        LocalDateTime expectedBeginDate = LocalDateTime.now().minus(5, ChronoUnit.DAYS);
        LocalDateTime expectedEndDate = LocalDateTime.now();
        ChannelHistory channelHistory = new ChannelHistory("channelId", true, "Invited", expectedBeginDate,
                expectedEndDate);
        ChannelHistoryInsertPreparedStatementSetter channelHistoryInsertPreparedStatementSetter =
                new ChannelHistoryInsertPreparedStatementSetter(channelHistory);

        channelHistoryInsertPreparedStatementSetter.setValues(preparedStatement);

        verify(preparedStatement, times(1)).setString(1, "channelId");
        verify(preparedStatement, times(1)).setString(2, "Y");
        verify(preparedStatement, times(1)).setString(3, "Invited");
        verify(preparedStatement, times(1)).setTimestamp(4, Timestamp.valueOf(expectedBeginDate));
        verify(preparedStatement, times(1)).setTimestamp(5, Timestamp.valueOf(expectedEndDate));
    }

    @Test
    void fieldsAreSetCorrectlyWhenMemberIsFalse() throws SQLException {
        LocalDateTime expectedBeginDate = LocalDateTime.now().minus(5, ChronoUnit.DAYS);
        LocalDateTime expectedEndDate = LocalDateTime.now();
        ChannelHistory channelHistory = new ChannelHistory("channelId", false, "Kicked", expectedBeginDate,
                expectedEndDate);
        ChannelHistoryInsertPreparedStatementSetter channelHistoryInsertPreparedStatementSetter =
                new ChannelHistoryInsertPreparedStatementSetter(channelHistory);

        channelHistoryInsertPreparedStatementSetter.setValues(preparedStatement);

        verify(preparedStatement, times(1)).setString(1, "channelId");
        verify(preparedStatement, times(1)).setString(2, "N");
        verify(preparedStatement, times(1)).setString(3, "Kicked");
        verify(preparedStatement, times(1)).setTimestamp(4, Timestamp.valueOf(expectedBeginDate));
        verify(preparedStatement, times(1)).setTimestamp(5, Timestamp.valueOf(expectedEndDate));
    }

}