package net.jackofalltrades.taterbot.channel;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.notNull;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.google.common.base.Optional;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.Lists;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@ExtendWith(MockitoExtension.class)
class ChannelManagerTest {

    @Mock
    private JdbcTemplate jdbcTemplate;

    private ChannelDao channelDao;
    private ChannelHistoryDao channelHistoryDao;
    private ChannelManager channelManager;

    @BeforeEach
    void setUpChannelManager() {
        channelDao = new ChannelDao(jdbcTemplate, new ChannelRowMapper());
        channelHistoryDao = new ChannelHistoryDao(jdbcTemplate, new ChannelHistoryRowMapper());

        ChannelCacheLoader channelCacheLoader = new ChannelCacheLoader(channelDao);
        LoadingCache<String, Channel> channelCache = CacheBuilder.newBuilder().build(channelCacheLoader);
        channelManager = spy(new ChannelManager(channelCache, channelDao, channelHistoryDao));
    }

    @Test
    void retrieveChannelFromCache() {
        Channel channel = new Channel("channelId", true, "Invited", LocalDateTime.now());

        doReturn(channel)
                .when(jdbcTemplate)
                .queryForObject(contains("from channel "), (ChannelRowMapper) notNull(), eq("channelId"));

        Optional<Channel> cachedChannel = channelManager.findChannelById("channelId");

        assertTrue(cachedChannel.isPresent(), "There should have been a channel returned.");
        assertSame(channel, cachedChannel.get(), "The returned channel does not match.");
    }

    @Test
    void retrieveChannelFromCacheFailed() {
        doThrow(new IncorrectResultSizeDataAccessException(1, 0))
                .when(jdbcTemplate)
                .queryForObject(contains("from channel "), (ChannelRowMapper) notNull(), eq("channelId"));

        Optional<Channel> cachedChannel = channelManager.findChannelById("channelId");

        assertFalse(cachedChannel.isPresent(), "There should not have been a channel returned.");
    }

    @Test
    void channelInsertedWhenFirstSeenWhileJoiningChannel() {
        doThrow(new IncorrectResultSizeDataAccessException(1, 0))
                .when(jdbcTemplate)
                .queryForObject(contains("from channel "), (ChannelRowMapper) notNull(), eq("channelId"));
        doReturn(1)
                .when(jdbcTemplate)
                .update(contains("into channel "), (ChannelInsertPreparedStatementSetter) notNull());

        channelManager.joinChannel("channelId");

        ArgumentCaptor<LocalDateTime> joinDateCaptor = ArgumentCaptor.forClass(LocalDateTime.class);
        verify(channelManager, times(1)).joinChannel(eq("channelId"), eq("Invited"), joinDateCaptor.capture());

        ArgumentCaptor<ChannelInsertPreparedStatementSetter> channelInsertPreparedStatementSetterCaptor =
                ArgumentCaptor.forClass(ChannelInsertPreparedStatementSetter.class);
        verify(jdbcTemplate, times(1))
                .update(contains("into channel "), channelInsertPreparedStatementSetterCaptor.capture());

        Channel insertedChannel = new Channel("channelId", true, "Invited", joinDateCaptor.getValue());
        assertEquals(new ChannelInsertPreparedStatementSetter(insertedChannel),
                channelInsertPreparedStatementSetterCaptor.getValue(),
                "The channel inserted does not match.");
    }

    @Test
    void channelInsertedWhenFirstSeenWhileLeavingChannel() {
        doThrow(new IncorrectResultSizeDataAccessException(1, 0))
                .when(jdbcTemplate)
                .queryForObject(contains("from channel "), (ChannelRowMapper) notNull(), eq("channelId"));
        doReturn(1)
                .when(jdbcTemplate)
                .update(contains("into channel "), (ChannelInsertPreparedStatementSetter) notNull());

        channelManager.leaveChannel("channelId");

        ArgumentCaptor<LocalDateTime> leaveDateCaptor = ArgumentCaptor.forClass(LocalDateTime.class);
        verify(channelManager, times(1)).leaveChannel(eq("channelId"), eq("Kicked"), leaveDateCaptor.capture());

        ArgumentCaptor<ChannelInsertPreparedStatementSetter> channelInsertPreparedStatementSetterCaptor =
                ArgumentCaptor.forClass(ChannelInsertPreparedStatementSetter.class);
        verify(jdbcTemplate, times(1))
                .update(contains("into channel "), channelInsertPreparedStatementSetterCaptor.capture());

        Channel insertedChannel = new Channel("channelId", false, "Kicked", leaveDateCaptor.getValue());
        assertEquals(new ChannelInsertPreparedStatementSetter(insertedChannel),
                channelInsertPreparedStatementSetterCaptor.getValue(),
                "The channel inserted does not match.");
    }

    @Test
    void channelJoinedWhenSeenBeforeAndNotCurrentlyJoined(@Mock LoadingCache<String, Channel> channelCache) {
        channelManager = spy(new ChannelManager(channelCache, channelDao, channelHistoryDao));

        LocalDateTime originalMembershipDate = LocalDateTime.now().minus(5, ChronoUnit.DAYS);
        Channel channel = new Channel("channelId", false, "Kicked", originalMembershipDate);
        doReturn(channel)
                .when(jdbcTemplate)
                .queryForObject(contains("from channel "), (ChannelRowMapper) notNull(), eq("channelId"));
        doReturn(1)
                .when(jdbcTemplate)
                .update(contains("update channel "), (ChannelUpdatePreparedStatementSetter) notNull());
        doReturn(1)
                .when(jdbcTemplate)
                .update(contains("into channel_history"), (ChannelHistoryInsertPreparedStatementSetter) notNull());

        channelManager.joinChannel("channelId");

        ArgumentCaptor<LocalDateTime> joinDateCaptor = ArgumentCaptor.forClass(LocalDateTime.class);
        verify(channelManager, times(1)).joinChannel(eq("channelId"), eq("Invited"), joinDateCaptor.capture());

        ArgumentCaptor<ChannelUpdatePreparedStatementSetter> channelUpdatePreparedStatementSetterCaptor =
                ArgumentCaptor.forClass(ChannelUpdatePreparedStatementSetter.class);
        verify(jdbcTemplate, times(1))
                .update(contains("update channel "), channelUpdatePreparedStatementSetterCaptor.capture());

        assertEquals(new ChannelUpdatePreparedStatementSetter(channel, true, "Invited", joinDateCaptor.getValue()),
                channelUpdatePreparedStatementSetterCaptor.getValue(),
                "The channel updated does not match.");

        ArgumentCaptor<ChannelHistoryInsertPreparedStatementSetter> channelHistoryInsertPreparedStatementSetterCaptor =
                ArgumentCaptor.forClass(ChannelHistoryInsertPreparedStatementSetter.class);
        verify(jdbcTemplate, times(1))
                .update(contains("into channel_history"), channelHistoryInsertPreparedStatementSetterCaptor.capture());

        ChannelHistory channelHistory = new ChannelHistory("channelId", false, "Kicked", originalMembershipDate,
                joinDateCaptor.getValue());
        assertEquals(new ChannelHistoryInsertPreparedStatementSetter(channelHistory),
                channelHistoryInsertPreparedStatementSetterCaptor.getValue(),
                "The channel history inserted does not match.");

        verify(channelCache, times(1)).refresh("channelId");
    }

    @Test
    void channelLeftWhenSeenBeforeAndCurrentlyJoined(@Mock LoadingCache<String, Channel> channelCache) {
        channelManager = spy(new ChannelManager(channelCache, channelDao, channelHistoryDao));

        LocalDateTime originalMembershipDate = LocalDateTime.now().minus(5, ChronoUnit.DAYS);
        Channel channel = new Channel("channelId", true, "Invited", originalMembershipDate);
        doReturn(channel)
                .when(jdbcTemplate)
                .queryForObject(contains("from channel "), (ChannelRowMapper) notNull(), eq("channelId"));
        doReturn(1)
                .when(jdbcTemplate)
                .update(contains("update channel "), (ChannelUpdatePreparedStatementSetter) notNull());
        doReturn(1)
                .when(jdbcTemplate)
                .update(contains("into channel_history"), (ChannelHistoryInsertPreparedStatementSetter) notNull());

        channelManager.leaveChannel("channelId");

        ArgumentCaptor<ChannelUpdatePreparedStatementSetter> channelUpdatePreparedStatementSetterCaptor =
                ArgumentCaptor.forClass(ChannelUpdatePreparedStatementSetter.class);
        verify(jdbcTemplate, times(1))
                .update(contains("update channel "), channelUpdatePreparedStatementSetterCaptor.capture());

        ArgumentCaptor<LocalDateTime> leaveDateCaptor = ArgumentCaptor.forClass(LocalDateTime.class);
        verify(channelManager, times(1)).leaveChannel(eq("channelId"), eq("Kicked"), leaveDateCaptor.capture());

        assertEquals(new ChannelUpdatePreparedStatementSetter(channel, false, "Kicked", leaveDateCaptor.getValue()),
                channelUpdatePreparedStatementSetterCaptor.getValue(),
                "The channel updated does not match.");

        ArgumentCaptor<ChannelHistoryInsertPreparedStatementSetter> channelHistoryInsertPreparedStatementSetterCaptor =
                ArgumentCaptor.forClass(ChannelHistoryInsertPreparedStatementSetter.class);
        verify(jdbcTemplate, times(1))
                .update(contains("into channel_history"), channelHistoryInsertPreparedStatementSetterCaptor.capture());

        ChannelHistory channelHistory = new ChannelHistory("channelId", true, "Invited", originalMembershipDate,
                leaveDateCaptor.getValue());
        assertEquals(new ChannelHistoryInsertPreparedStatementSetter(channelHistory),
                channelHistoryInsertPreparedStatementSetterCaptor.getValue(),
                "The channel history inserted does not match.");

        verify(channelCache, times(1)).refresh("channelId");
    }

    @Test
    void findChannelHistoryByChannelId() {
        List<ChannelHistory> channelHistoryList = Lists.newArrayList(
                new ChannelHistory("channelId", true, "Invited", LocalDateTime.now().minus(5, ChronoUnit.MINUTES),
                        LocalDateTime.now()),
                new ChannelHistory("channelId", false, "Kicked", LocalDateTime.now().minus(10, ChronoUnit.MINUTES),
                        LocalDateTime.now().minus(5, ChronoUnit.MINUTES)));
        doReturn(channelHistoryList)
                .when(jdbcTemplate)
                .query(contains("from channel_history"), (ChannelHistoryRowMapper) notNull(), eq("channelId"));

        List<ChannelHistory> actualChannelHistoryList = channelManager.findHistoryForChannelId("channelId");
        assertSame(channelHistoryList, actualChannelHistoryList, "The channel history list does not match.");
    }

}
