package net.jackofalltrades.taterbot.service;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.LocalDateTime;

@ExtendWith(MockitoExtension.class)
class ChannelServiceUpdatePreparedStatementCreatorTest {

    @Mock
    private Connection connection;

    @Mock
    private PreparedStatement preparedStatement;

    @Test
    void createPreparedStatementForChannelServiceWithoutUserId() throws SQLException {
        doReturn(preparedStatement).when(connection).prepareStatement(contains("user_id is null"));

        LocalDateTime currentDateTime = LocalDateTime.now();
        ChannelService channelService = new ChannelService("channelId", "service", Service.Status.INACTIVE,
                currentDateTime, null);
        ChannelServiceUpdatePreparedStatementCreator channelServiceUpdatePreparedStatementCreator =
                new ChannelServiceUpdatePreparedStatementCreator(channelService, Service.Status.ACTIVE, "updatingUser");

        PreparedStatement createdPreparedStatement =
                channelServiceUpdatePreparedStatementCreator.createPreparedStatement(connection);
        assertSame(preparedStatement, createdPreparedStatement, "The prepared statement does not match.");

        verify(preparedStatement, times(1)).setString(1, "active");
        verify(preparedStatement, times(1)).setString(2, "updatingUser");
        verify(preparedStatement, times(1)).setString(3, "channelId");
        verify(preparedStatement, times(1)).setString(4, "service");
        verify(preparedStatement, times(1)).setString(5, "inactive");
        verify(preparedStatement, times(1)).setTimestamp(6, Timestamp.valueOf(currentDateTime));
        verifyNoMoreInteractions(preparedStatement);
    }

    @Test
    void createPreparedStatementForChannelServiceWithUserId() throws SQLException {
        doReturn(preparedStatement).when(connection).prepareStatement(contains("and user_id = ?"));

        LocalDateTime currentDateTime = LocalDateTime.now();
        ChannelService channelService = new ChannelService("channelId", "service", Service.Status.INACTIVE,
                currentDateTime, "lastUpdatedUserId");
        ChannelServiceUpdatePreparedStatementCreator channelServiceUpdatePreparedStatementCreator =
                new ChannelServiceUpdatePreparedStatementCreator(channelService, Service.Status.DISABLED,
                        "updatingUser");

        PreparedStatement createdPreparedStatement =
                channelServiceUpdatePreparedStatementCreator.createPreparedStatement(connection);
        assertSame(preparedStatement, createdPreparedStatement, "The prepared statement does not match.");

        verify(preparedStatement, times(1)).setString(1, "disabled");
        verify(preparedStatement, times(1)).setString(2, "updatingUser");
        verify(preparedStatement, times(1)).setString(3, "channelId");
        verify(preparedStatement, times(1)).setString(4, "service");
        verify(preparedStatement, times(1)).setString(5, "inactive");
        verify(preparedStatement, times(1)).setTimestamp(6, Timestamp.valueOf(currentDateTime));
        verify(preparedStatement, times(1)).setString(7, "lastUpdatedUserId");
        verifyNoMoreInteractions(preparedStatement);
    }

    @Test
    void createPreparedStatementForChannelServiceWhenUpdatingUserIdIsNull() throws SQLException {
        doReturn(preparedStatement).when(connection).prepareStatement(contains("and user_id = ?"));

        LocalDateTime currentDateTime = LocalDateTime.now();
        ChannelService channelService = new ChannelService("channelId", "service", Service.Status.DISABLED,
                currentDateTime, "lastUpdatedUserId");
        ChannelServiceUpdatePreparedStatementCreator channelServiceUpdatePreparedStatementCreator =
                new ChannelServiceUpdatePreparedStatementCreator(channelService, Service.Status.INACTIVE);

        PreparedStatement createdPreparedStatement =
                channelServiceUpdatePreparedStatementCreator.createPreparedStatement(connection);
        assertSame(preparedStatement, createdPreparedStatement, "The prepared statement does not match.");

        verify(preparedStatement, times(1)).setString(1, "inactive");
        verify(preparedStatement, times(1)).setNull(2, Types.VARCHAR);
        verify(preparedStatement, times(1)).setString(3, "channelId");
        verify(preparedStatement, times(1)).setString(4, "service");
        verify(preparedStatement, times(1)).setString(5, "disabled");
        verify(preparedStatement, times(1)).setTimestamp(6, Timestamp.valueOf(currentDateTime));
        verify(preparedStatement, times(1)).setString(7, "lastUpdatedUserId");
        verifyNoMoreInteractions(preparedStatement);
    }

    @Test
    void createPreparedStatementForChannelServiceWhenUpdatingUserIdIsEmpty() throws SQLException {
        doReturn(preparedStatement).when(connection).prepareStatement(contains("and user_id = ?"));

        LocalDateTime currentDateTime = LocalDateTime.now();
        ChannelService channelService = new ChannelService("channelId", "service", Service.Status.INACTIVE,
                currentDateTime, "lastUpdatedUserId");
        ChannelServiceUpdatePreparedStatementCreator channelServiceUpdatePreparedStatementCreator =
                new ChannelServiceUpdatePreparedStatementCreator(channelService, Service.Status.ACTIVE, "");

        PreparedStatement createdPreparedStatement =
                channelServiceUpdatePreparedStatementCreator.createPreparedStatement(connection);
        assertSame(preparedStatement, createdPreparedStatement, "The prepared statement does not match.");

        verify(preparedStatement, times(1)).setString(1, "active");
        verify(preparedStatement, times(1)).setNull(2, Types.VARCHAR);
        verify(preparedStatement, times(1)).setString(3, "channelId");
        verify(preparedStatement, times(1)).setString(4, "service");
        verify(preparedStatement, times(1)).setString(5, "inactive");
        verify(preparedStatement, times(1)).setTimestamp(6, Timestamp.valueOf(currentDateTime));
        verify(preparedStatement, times(1)).setString(7, "lastUpdatedUserId");
        verifyNoMoreInteractions(preparedStatement);
    }

}
