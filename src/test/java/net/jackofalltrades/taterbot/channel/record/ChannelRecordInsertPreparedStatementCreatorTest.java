package net.jackofalltrades.taterbot.channel.record;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;

@ExtendWith(MockitoExtension.class)
class ChannelRecordInsertPreparedStatementCreatorTest {

    @Mock
    private Connection connection;

    @Mock
    private PreparedStatement preparedStatement;

    @Test
    void channelRecordDataSetWhenUserInformationPresent() throws SQLException {
        LocalDateTime messageTimestamp = LocalDateTime.now();
        ChannelRecord channelRecord = new ChannelRecord("channelId", "userId", "displayName", "messageType",
                messageTimestamp, "message");
        ChannelRecordInsertPreparedStatementCreator channelRecordInsertPreparedStatementCreator =
                new ChannelRecordInsertPreparedStatementCreator(channelRecord);

        doReturn(preparedStatement).when(connection).prepareStatement(
                "insert into channel_record (channel_id, user_id, user_display_name, message_type, message_timestamp," +
                        " message) values (?, ?, ?, ?, ?, ?)");

        channelRecordInsertPreparedStatementCreator.createPreparedStatement(connection);

        verify(preparedStatement, times(1)).setString(1, "channelId");
        verify(preparedStatement, times(1)).setString(2, "userId");
        verify(preparedStatement, times(1)).setString(3, "displayName");
        verify(preparedStatement, times(1)).setString(4, "messageType");
        verify(preparedStatement, times(1)).setTimestamp(5, Timestamp.valueOf(messageTimestamp));
        verify(preparedStatement, times(1)).setString(6, "message");

        ChannelRecord expectedChannelRecord = new ChannelRecord("channelId", "userId", "displayName", "messageType",
                messageTimestamp, "message");
        assertEquals(new ChannelRecordInsertPreparedStatementCreator(expectedChannelRecord),
                channelRecordInsertPreparedStatementCreator, "The channel record insert creator does not match.");
    }

    @Test
    void channelRecordDataSetWhenUserInformationMissing() throws SQLException {
        LocalDateTime messageTimestamp = LocalDateTime.now();
        ChannelRecord channelRecord = new ChannelRecord("channelId", null, null, "messageType", messageTimestamp,
                "message");
        ChannelRecordInsertPreparedStatementCreator channelRecordInsertPreparedStatementCreator =
                new ChannelRecordInsertPreparedStatementCreator(channelRecord);

        doReturn(preparedStatement).when(connection).prepareStatement(
                "insert into channel_record (channel_id, message_type, message_timestamp, message) values (?, ?, ?, ?)");

        channelRecordInsertPreparedStatementCreator.createPreparedStatement(connection);

        verify(preparedStatement, times(1)).setString(1, "channelId");
        verify(preparedStatement, times(1)).setString(2, "messageType");
        verify(preparedStatement, times(1)).setTimestamp(3, Timestamp.valueOf(messageTimestamp));
        verify(preparedStatement, times(1)).setString(4, "message");

        ChannelRecord expectedChannelRecord = new ChannelRecord("channelId", null, null, "messageType",
                messageTimestamp, "message");
        assertEquals(new ChannelRecordInsertPreparedStatementCreator(expectedChannelRecord),
                channelRecordInsertPreparedStatementCreator, "The channel record insert creator does not match.");
    }

    @Test
    void channelRecordDataSetWhenUserIdIsPresentAndDisplayNameIsMissing() throws SQLException {
        LocalDateTime messageTimestamp = LocalDateTime.now();
        ChannelRecord channelRecord = new ChannelRecord("channelId", "userId", null, "messageType", messageTimestamp,
                "message");
        ChannelRecordInsertPreparedStatementCreator channelRecordInsertPreparedStatementCreator =
                new ChannelRecordInsertPreparedStatementCreator(channelRecord);

        doReturn(preparedStatement).when(connection).prepareStatement(
                "insert into channel_record (channel_id, user_id, message_type, message_timestamp, message) values " +
                        "(?, ?, ?, ?, ?)");

        channelRecordInsertPreparedStatementCreator.createPreparedStatement(connection);

        verify(preparedStatement, times(1)).setString(1, "channelId");
        verify(preparedStatement, times(1)).setString(2, "userId");
        verify(preparedStatement, times(1)).setString(3, "messageType");
        verify(preparedStatement, times(1)).setTimestamp(4, Timestamp.valueOf(messageTimestamp));
        verify(preparedStatement, times(1)).setString(5, "message");

        ChannelRecord expectedChannelRecord = new ChannelRecord("channelId", "userId", null, "messageType",
                messageTimestamp, "message");
        assertEquals(new ChannelRecordInsertPreparedStatementCreator(expectedChannelRecord),
                channelRecordInsertPreparedStatementCreator, "The channel record insert creator does not match.");
    }

    @Test
    void channelRecordDataSetWhenUserIdIsMissingAndDisplayNameIsPresent() throws SQLException {
        LocalDateTime messageTimestamp = LocalDateTime.now();
        ChannelRecord channelRecord = new ChannelRecord("channelId", null, "displayName", "messageType",
                messageTimestamp, "message");
        ChannelRecordInsertPreparedStatementCreator channelRecordInsertPreparedStatementCreator =
                new ChannelRecordInsertPreparedStatementCreator(channelRecord);

        doReturn(preparedStatement).when(connection).prepareStatement(
                "insert into channel_record (channel_id, message_type, message_timestamp, message) values (?, ?, ?, ?)");

        channelRecordInsertPreparedStatementCreator.createPreparedStatement(connection);

        verify(preparedStatement, times(1)).setString(1, "channelId");
        verify(preparedStatement, times(1)).setString(2, "messageType");
        verify(preparedStatement, times(1)).setTimestamp(3, Timestamp.valueOf(messageTimestamp));
        verify(preparedStatement, times(1)).setString(4, "message");

        ChannelRecord expectedChannelRecord = new ChannelRecord("channelId", null, "displayName", "messageType",
                messageTimestamp, "message");
        assertEquals(new ChannelRecordInsertPreparedStatementCreator(expectedChannelRecord),
                channelRecordInsertPreparedStatementCreator, "The channel record insert creator does not match.");
    }

}