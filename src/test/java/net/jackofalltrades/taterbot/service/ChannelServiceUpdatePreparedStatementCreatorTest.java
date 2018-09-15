package net.jackofalltrades.taterbot.service;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.contains;
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
import java.time.temporal.ChronoUnit;

@ExtendWith(MockitoExtension.class)
class ChannelServiceUpdatePreparedStatementCreatorTest {

    @Mock
    private Connection connection;

    @Mock
    private PreparedStatement preparedStatement;

    @Test
    void createPreparedStatementForChannelServiceWithoutUserId() throws SQLException {
        doReturn(preparedStatement).when(connection).prepareStatement(contains("user_id is null"));

        LocalDateTime originalStatusDate = LocalDateTime.now().minus(5, ChronoUnit.DAYS);
        LocalDateTime updatedStatusDate = LocalDateTime.now();
        ChannelService channelService = new ChannelService("channelId", "service", Service.Status.INACTIVE,
                originalStatusDate, null);
        ChannelServiceUpdatePreparedStatementCreator channelServiceUpdatePreparedStatementCreator =
                new ChannelServiceUpdatePreparedStatementCreator(channelService, Service.Status.ACTIVE,
                        updatedStatusDate, "updatingUser");

        PreparedStatement createdPreparedStatement =
                channelServiceUpdatePreparedStatementCreator.createPreparedStatement(connection);
        assertSame(preparedStatement, createdPreparedStatement, "The prepared statement does not match.");

        verify(preparedStatement, times(1)).setString(1, "active");
        verify(preparedStatement, times(1)).setTimestamp(2, Timestamp.valueOf(updatedStatusDate));
        verify(preparedStatement, times(1)).setString(3, "updatingUser");
        verify(preparedStatement, times(1)).setString(4, "channelId");
        verify(preparedStatement, times(1)).setString(5, "service");
        verify(preparedStatement, times(1)).setString(6, "inactive");
        verify(preparedStatement, times(1)).setTimestamp(7, Timestamp.valueOf(originalStatusDate));
        verifyNoMoreInteractions(preparedStatement);
    }

    @Test
    void createPreparedStatementForChannelServiceWithUserId() throws SQLException {
        doReturn(preparedStatement).when(connection).prepareStatement(contains("and user_id = ?"));

        LocalDateTime originalStatusDate = LocalDateTime.now().minus(3, ChronoUnit.DAYS);
        LocalDateTime updatedStatusDate = LocalDateTime.now();
        ChannelService channelService = new ChannelService("channelId", "service", Service.Status.INACTIVE,
                originalStatusDate, "lastUpdatedUserId");
        ChannelServiceUpdatePreparedStatementCreator channelServiceUpdatePreparedStatementCreator =
                new ChannelServiceUpdatePreparedStatementCreator(channelService, Service.Status.DISABLED,
                        updatedStatusDate, "updatingUser");

        PreparedStatement createdPreparedStatement =
                channelServiceUpdatePreparedStatementCreator.createPreparedStatement(connection);
        assertSame(preparedStatement, createdPreparedStatement, "The prepared statement does not match.");

        verify(preparedStatement, times(1)).setString(1, "disabled");
        verify(preparedStatement, times(1)).setTimestamp(2, Timestamp.valueOf(updatedStatusDate));
        verify(preparedStatement, times(1)).setString(3, "updatingUser");
        verify(preparedStatement, times(1)).setString(4, "channelId");
        verify(preparedStatement, times(1)).setString(5, "service");
        verify(preparedStatement, times(1)).setString(6, "inactive");
        verify(preparedStatement, times(1)).setTimestamp(7, Timestamp.valueOf(originalStatusDate));
        verify(preparedStatement, times(1)).setString(8, "lastUpdatedUserId");
        verifyNoMoreInteractions(preparedStatement);
    }

    @Test
    void createPreparedStatementForChannelServiceWhenUpdatingUserIdIsNull() throws SQLException {
        doReturn(preparedStatement).when(connection).prepareStatement(contains("and user_id = ?"));

        LocalDateTime originalStatusDate = LocalDateTime.now().minus(1, ChronoUnit.HOURS);
        LocalDateTime updatedStatusDate = LocalDateTime.now();
        ChannelService channelService = new ChannelService("channelId", "service", Service.Status.DISABLED,
                originalStatusDate, "lastUpdatedUserId");
        ChannelServiceUpdatePreparedStatementCreator channelServiceUpdatePreparedStatementCreator =
                new ChannelServiceUpdatePreparedStatementCreator(channelService, Service.Status.INACTIVE,
                        updatedStatusDate);

        PreparedStatement createdPreparedStatement =
                channelServiceUpdatePreparedStatementCreator.createPreparedStatement(connection);
        assertSame(preparedStatement, createdPreparedStatement, "The prepared statement does not match.");

        verify(preparedStatement, times(1)).setString(1, "inactive");
        verify(preparedStatement, times(1)).setTimestamp(2, Timestamp.valueOf(updatedStatusDate));
        verify(preparedStatement, times(1)).setNull(3, Types.VARCHAR);
        verify(preparedStatement, times(1)).setString(4, "channelId");
        verify(preparedStatement, times(1)).setString(5, "service");
        verify(preparedStatement, times(1)).setString(6, "disabled");
        verify(preparedStatement, times(1)).setTimestamp(7, Timestamp.valueOf(originalStatusDate));
        verify(preparedStatement, times(1)).setString(8, "lastUpdatedUserId");
        verifyNoMoreInteractions(preparedStatement);
    }

    @Test
    void createPreparedStatementForChannelServiceWhenUpdatingUserIdIsEmpty() throws SQLException {
        doReturn(preparedStatement).when(connection).prepareStatement(contains("and user_id = ?"));

        LocalDateTime originalStatusDate = LocalDateTime.now().minus(2, ChronoUnit.MINUTES);
        LocalDateTime updatedStatusDate = LocalDateTime.now();
        ChannelService channelService = new ChannelService("channelId", "service", Service.Status.INACTIVE,
                originalStatusDate, "lastUpdatedUserId");
        ChannelServiceUpdatePreparedStatementCreator channelServiceUpdatePreparedStatementCreator =
                new ChannelServiceUpdatePreparedStatementCreator(channelService, Service.Status.ACTIVE,
                        updatedStatusDate, "");

        PreparedStatement createdPreparedStatement =
                channelServiceUpdatePreparedStatementCreator.createPreparedStatement(connection);
        assertSame(preparedStatement, createdPreparedStatement, "The prepared statement does not match.");

        verify(preparedStatement, times(1)).setString(1, "active");
        verify(preparedStatement, times(1)).setTimestamp(2, Timestamp.valueOf(updatedStatusDate));
        verify(preparedStatement, times(1)).setNull(3, Types.VARCHAR);
        verify(preparedStatement, times(1)).setString(4, "channelId");
        verify(preparedStatement, times(1)).setString(5, "service");
        verify(preparedStatement, times(1)).setString(6, "inactive");
        verify(preparedStatement, times(1)).setTimestamp(7, Timestamp.valueOf(originalStatusDate));
        verify(preparedStatement, times(1)).setString(8, "lastUpdatedUserId");
        verifyNoMoreInteractions(preparedStatement);
    }

}
