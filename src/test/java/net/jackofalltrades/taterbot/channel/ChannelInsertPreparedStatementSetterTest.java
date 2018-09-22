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

@ExtendWith(MockitoExtension.class)
class ChannelInsertPreparedStatementSetterTest {

    @Mock
    private PreparedStatement preparedStatement;

    @Test
    void setAllFieldsOnStatementWithoutMembership() throws SQLException {
        LocalDateTime membershipDate = LocalDateTime.now();
        Channel channel = new Channel("channelId", false, "Kicked", membershipDate);
        ChannelInsertPreparedStatementSetter channelInsertPreparedStatementSetter =
                new ChannelInsertPreparedStatementSetter(channel);

        channelInsertPreparedStatementSetter.setValues(preparedStatement);

        verify(preparedStatement, times(1)).setString(1, "channelId");
        verify(preparedStatement, times(1)).setString(2, "N");
        verify(preparedStatement, times(1)).setString(3, "Kicked");
        verify(preparedStatement, times(1)).setTimestamp(4, Timestamp.valueOf(membershipDate));
    }

    @Test
    void setAllFieldsOnStatementWithMembership() throws SQLException {
        LocalDateTime membershipDate = LocalDateTime.now();
        Channel channel = new Channel("channelId", true, "Invited", membershipDate);
        ChannelInsertPreparedStatementSetter channelInsertPreparedStatementSetter =
                new ChannelInsertPreparedStatementSetter(channel);

        channelInsertPreparedStatementSetter.setValues(preparedStatement);

        verify(preparedStatement, times(1)).setString(1, "channelId");
        verify(preparedStatement, times(1)).setString(2, "Y");
        verify(preparedStatement, times(1)).setString(3, "Invited");
        verify(preparedStatement, times(1)).setTimestamp(4, Timestamp.valueOf(membershipDate));
    }

}