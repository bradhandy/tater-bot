package net.jackofalltrades.taterbot.channel;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.AdditionalMatchers.and;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.notNull;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.IncorrectUpdateSemanticsDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@ExtendWith(MockitoExtension.class)
class ChannelDaoTest {

    @Mock
    private JdbcTemplate jdbcTemplate;

    private ChannelDao channelDao;

    @BeforeEach
    void createChannelDao() {
        channelDao = new ChannelDao(jdbcTemplate, new ChannelRowMapper());
    }

    @Test
    void retrieveChannelById() {
        Channel expectedChannel = new Channel("channelId", false, "Kicked", LocalDateTime.now());

        doReturn(expectedChannel)
                .when(jdbcTemplate)
                .queryForObject(and(contains("from channel"), contains("channel_id = ?")),
                        (RowMapper<Channel>) notNull(), eq("channelId"));

        Channel actualChannel = channelDao.findChannel("channelId");
        assertSame(expectedChannel, actualChannel, "The channel did not match the expected channel.");
    }

    @Test
    void insertChannelSucceeds() {
        Channel newChannel = new Channel("channelId", true, "Invited", LocalDateTime.now().minus(5, ChronoUnit.HOURS));

        doReturn(1)
                .when(jdbcTemplate)
                .update(and(contains("into channel"), contains("values (?, ?, ?, ?)")),
                        (ChannelInsertPreparedStatementSetter) notNull());

        channelDao.insertChannel(newChannel);

        ArgumentCaptor<ChannelInsertPreparedStatementSetter> channelInsertPreparedStatementSetterCaptor =
                ArgumentCaptor.forClass(ChannelInsertPreparedStatementSetter.class);
        verify(jdbcTemplate, times(1)).update(and(contains("into channel"), contains("values (?, ?, ?, ?)")),
                channelInsertPreparedStatementSetterCaptor.capture());

        ChannelInsertPreparedStatementSetter channelInsertPreparedStatementSetter =
                channelInsertPreparedStatementSetterCaptor.getValue();
        assertEquals(new ChannelInsertPreparedStatementSetter(newChannel), channelInsertPreparedStatementSetter,
                "The insert prepared statement setter does not match.");
    }

    @Test
    void insertChannelFails() {
        Channel newChannel = new Channel("failedChannelId", true, "Invited", LocalDateTime.now());

        assertThrows(IncorrectUpdateSemanticsDataAccessException.class, () -> channelDao.insertChannel(newChannel));

        ArgumentCaptor<ChannelInsertPreparedStatementSetter> channelInsertPreparedStatementSetterCaptor =
                ArgumentCaptor.forClass(ChannelInsertPreparedStatementSetter.class);
        verify(jdbcTemplate, times(1)).update(and(contains("into channel"), contains("values (?, ?, ?, ?)")),
                channelInsertPreparedStatementSetterCaptor.capture());

        ChannelInsertPreparedStatementSetter channelInsertPreparedStatementSetter =
                channelInsertPreparedStatementSetterCaptor.getValue();
        assertEquals(new ChannelInsertPreparedStatementSetter(newChannel), channelInsertPreparedStatementSetter,
                "The insert prepared statement setter does not match.");
    }

    @Test
    void updateChannelSucceeds() {
        LocalDateTime updatedMembershipTime = LocalDateTime.now();
        Channel existingChannel =
                new Channel("channelId", true, "Invited", updatedMembershipTime.minus(5, ChronoUnit.MINUTES));

        doReturn(1)
                .when(jdbcTemplate)
                .update(and(contains("update channel "), contains("where channel_id = ?")),
                        (ChannelUpdatePreparedStatementSetter) notNull());

        assertTrue(channelDao.updateChannel(existingChannel, false, "Kicked", updatedMembershipTime),
                "The channel should have been updated.");

        ArgumentCaptor<ChannelUpdatePreparedStatementSetter> channelUpdatePreparedStatementSetterCaptor =
                ArgumentCaptor.forClass(ChannelUpdatePreparedStatementSetter.class);
        verify(jdbcTemplate, times(1)).update(and(contains("update channel "), contains("where channel_id = ?")),
                channelUpdatePreparedStatementSetterCaptor.capture());

        assertEquals(new ChannelUpdatePreparedStatementSetter(existingChannel, false, "Kicked", updatedMembershipTime),
                channelUpdatePreparedStatementSetterCaptor.getValue(),
                "The updated prepared statement setter does not match.");
    }

    @Test
    void updateChannelFails() {
        LocalDateTime updatedMembershipTime = LocalDateTime.now();
        Channel existingChannel =
                new Channel("channelId", true, "Invited", updatedMembershipTime.minus(5, ChronoUnit.MINUTES));

        assertFalse(channelDao.updateChannel(existingChannel, false, "Kicked", updatedMembershipTime),
                "The channel should not have been updated.");

        ArgumentCaptor<ChannelUpdatePreparedStatementSetter> channelUpdatePreparedStatementSetterCaptor =
                ArgumentCaptor.forClass(ChannelUpdatePreparedStatementSetter.class);
        verify(jdbcTemplate, times(1)).update(and(contains("update channel "), contains("where channel_id = ?")),
                channelUpdatePreparedStatementSetterCaptor.capture());

        assertEquals(new ChannelUpdatePreparedStatementSetter(existingChannel, false, "Kicked", updatedMembershipTime),
                channelUpdatePreparedStatementSetterCaptor.getValue(),
                "The updated prepared statement setter does not match.");
    }

    @Test
    void updateChannelFailForTooManyUpdates() {
        LocalDateTime updatedMembershipTime = LocalDateTime.now();
        Channel existingChannel =
                new Channel("channelId", true, "Invited", updatedMembershipTime.minus(5, ChronoUnit.MINUTES));

        doReturn(2)
                .when(jdbcTemplate)
                .update(and(contains("update channel "), contains("where channel_id = ?")),
                        (ChannelUpdatePreparedStatementSetter) notNull());

        assertThrows(IncorrectUpdateSemanticsDataAccessException.class,
                () -> channelDao.updateChannel(existingChannel, false, "Kicked", updatedMembershipTime),
                "The update should have failed for too many updates.");

        ArgumentCaptor<ChannelUpdatePreparedStatementSetter> channelUpdatePreparedStatementSetterCaptor =
                ArgumentCaptor.forClass(ChannelUpdatePreparedStatementSetter.class);
        verify(jdbcTemplate, times(1)).update(and(contains("update channel "), contains("where channel_id = ?")),
                channelUpdatePreparedStatementSetterCaptor.capture());

        assertEquals(new ChannelUpdatePreparedStatementSetter(existingChannel, false, "Kicked", updatedMembershipTime),
                channelUpdatePreparedStatementSetterCaptor.getValue(),
                "The updated prepared statement setter does not match.");
    }

}
