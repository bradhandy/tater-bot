package net.jackofalltrades.taterbot.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.AdditionalMatchers.and;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.notNull;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.google.common.base.Optional;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.ImmutableMap;
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
import java.util.List;
import java.util.Map;

@ExtendWith(MockitoExtension.class)
class ChannelServiceManagerTest {

    private static final ChannelService DATABASE_CHANNEL_SERVICE =
            new ChannelService("channelId", "service", Service.Status.ACTIVE, LocalDateTime.now(), "userId");

    @Mock private JdbcTemplate jdbcTemplate;

    private ChannelServiceManager channelServiceManager;
    private ChannelServiceHistoryDao channelServiceHistoryDao;
    private ChannelServiceDao channelServiceDao;

    @BeforeEach
    void setUpChannelServiceManager() {
        ServiceDao serviceDao = new ServiceDao(jdbcTemplate, new ServiceRowMapper());
        ServiceCacheLoader serviceCacheLoader = new ServiceCacheLoader(serviceDao);
        LoadingCache<String, Service> serviceCache = CacheBuilder.newBuilder().build(serviceCacheLoader);

        channelServiceHistoryDao = new ChannelServiceHistoryDao(jdbcTemplate);
        channelServiceDao = new ChannelServiceDao(jdbcTemplate, new ChannelServiceRowMapper());

        ChannelServiceCacheLoader channelServiceCacheLoader = new ChannelServiceCacheLoader(channelServiceDao);
        LoadingCache<ChannelServiceKey, ChannelService> channelServiceCache =
                CacheBuilder.newBuilder().build(channelServiceCacheLoader);
        channelServiceManager = new ChannelServiceManager(channelServiceCache, channelServiceDao,
                channelServiceHistoryDao, serviceCache);
    }

    @Test
    void retrieveChannelServiceFromCache() {
        doReturn(DATABASE_CHANNEL_SERVICE).when(jdbcTemplate)
                .queryForObject(and(contains("select"), contains("from channel_service")),
                        (ChannelServiceRowMapper) notNull(), eq("channelId"), eq("service"));

        Optional<ChannelService> optionalChannelService =
                channelServiceManager.findChannelServiceByKey(new ChannelServiceKey("channelId", "service"));
        assertTrue(optionalChannelService.isPresent(), "The should have been a channel service returned.");
        assertSame(DATABASE_CHANNEL_SERVICE, optionalChannelService.get(), "The channel service does not match.");

        optionalChannelService = channelServiceManager.findChannelServiceByKey(
                new ChannelServiceKey("channelId", "service"));
        assertSame(DATABASE_CHANNEL_SERVICE, optionalChannelService.get(), "The channel service does not match.");

        verify(jdbcTemplate, times(1)).queryForObject(and(contains("select"), contains("from channel_service")),
                (ChannelServiceRowMapper) notNull(), eq("channelId"), eq("service"));
    }

    @Test
    void retrieveAbsentChannelServiceWhenNotInCache() {
        doThrow(new IncorrectResultSizeDataAccessException(1, 0))
                .when(jdbcTemplate)
                .queryForObject(and(contains("select"), contains("from channel_service")),
                        (ChannelServiceRowMapper) notNull(), eq("channelId"), eq("service"));

        Optional<ChannelService> optionalChannelService =
                channelServiceManager.findChannelServiceByKey(new ChannelServiceKey("channelId", "service"));
        assertFalse(optionalChannelService.isPresent(), "The should not have been a channel service returned.");
    }

    @Test
    void addMissingServicesToChannel() {
        LocalDateTime earliestAllowedStatusDate = LocalDateTime.now();
        Map<String, Service> codeToServiceMap = ImmutableMap.of(
                "service", new Service("service", "description", Service.Status.ACTIVE, LocalDateTime.now(),
                        Service.Status.INACTIVE),
                "missing", new Service("missing", "description 2", Service.Status.INACTIVE, LocalDateTime.now(),
                        Service.Status.ACTIVE));

        doReturn(Lists.newArrayList(codeToServiceMap.keySet()))
                .when(jdbcTemplate)
                .query(and(contains("select code"), contains("service left outer join")),
                        (StringColumnListResultSetExtractor) notNull(),
                        eq("channelId"));
        doReturn(1)
                .when(jdbcTemplate)
                .update(contains("insert into channel_service ("),
                        (ChannelServiceInsertPreparedStatementSetter) notNull());
        doReturn(codeToServiceMap.get("service"))
                .when(jdbcTemplate)
                .queryForObject(contains("from service"), (ServiceRowMapper) notNull(), eq("service"));
        doReturn(codeToServiceMap.get("missing"))
                .when(jdbcTemplate)
                .queryForObject(contains("from service"), (ServiceRowMapper) notNull(), eq("missing"));

        channelServiceManager.addMissingServicesToChannel("channelId");

        ArgumentCaptor<ChannelServiceInsertPreparedStatementSetter> channelServiceInsertPreparedStatementSetterCaptor =
                ArgumentCaptor.forClass(ChannelServiceInsertPreparedStatementSetter.class);
        verify(jdbcTemplate, times(1))
                .query(and(contains("select code"), contains("service left outer join")),
                        (StringColumnListResultSetExtractor) notNull(), eq("channelId"));
        verify(jdbcTemplate, times(2))
                .update(contains("insert into channel_service ("),
                        channelServiceInsertPreparedStatementSetterCaptor.capture());

        for (ChannelServiceInsertPreparedStatementSetter channelServiceInsertPreparedStatementSetter :
                channelServiceInsertPreparedStatementSetterCaptor.getAllValues()) {
            ChannelService channelService = channelServiceInsertPreparedStatementSetter.getChannelService();
            Service service = codeToServiceMap.get(channelService.getServiceCode());

            assertNotNull(service, "The service should be valid.");
            assertEquals(service.getInitialChannelStatus(), channelService.getStatus(),
                    "The channel status does not match the initial channel status of the service.");
            assertFalse(channelService.getStatusDate().isBefore(earliestAllowedStatusDate),
                    "The channel status date is not within the expected bounds.");
            assertNull(channelService.getUserId(), "The user id should be missing.");
        }
    }

}
