package net.jackofalltrades.taterbot.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.notNull;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.google.common.cache.LoadingCache;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.IncorrectUpdateSemanticsDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import java.time.LocalDateTime;

@ExtendWith(MockitoExtension.class)
class ChannelServiceDaoTest {

    public static final String CHANNEL_SERVICE_INSERT_SQL =
            "insert into channel_service (channel_id, service_code, status, status_date, user_id) " +
                    "values (?, ?, ?, ?, ?)";
    @Mock
    private JdbcTemplate jdbcTemplate;

    private ChannelServiceDao channelServiceDao;

    @BeforeEach
    void setUpChannelServiceDao() {
        channelServiceDao = new ChannelServiceDao(jdbcTemplate, new ChannelServiceRowMapper());
    }

    @Test
    void retrieveByChannelServiceKey() {
        ChannelServiceKey channelServiceKey = new ChannelServiceKey("channelId", "service");
        ChannelService databaseChannelService = new ChannelService("channelId", "service", Service.Status.ACTIVE,
                LocalDateTime.now(), "userId");

        doReturn(databaseChannelService).when(jdbcTemplate)
                .queryForObject(eq("select * from channel_service where channel_id = ? and service_code = ?"),
                        (ChannelServiceRowMapper) notNull(), eq("channelId"), eq("service"));

        ChannelService channelService = channelServiceDao.findChannelService(channelServiceKey);
        assertSame(databaseChannelService, channelService, "The channel service did not match.");
    }

    @Test
    void insertNewChannelService() {
        ChannelService channelService = new ChannelService("channelId", "service", Service.Status.ACTIVE,
                LocalDateTime.now(), "userId");

        doReturn(1).when(jdbcTemplate).update(eq(CHANNEL_SERVICE_INSERT_SQL),
                (ChannelServiceInsertPreparedStatementSetter) notNull());

        channelServiceDao.insertChannelService(channelService);

        ArgumentCaptor<ChannelServiceInsertPreparedStatementSetter> channelServiceInsertPreparedStatementSetterCaptor =
                ArgumentCaptor.forClass(ChannelServiceInsertPreparedStatementSetter.class);
        verify(jdbcTemplate, times(1)).update(eq(CHANNEL_SERVICE_INSERT_SQL),
                channelServiceInsertPreparedStatementSetterCaptor.capture());

        assertSame(channelService, channelServiceInsertPreparedStatementSetterCaptor.getValue().getChannelService(),
                "The channel service inserted does not match.");
    }

    @Test
    void insertNewChannelServiceFails() {
        ChannelService channelService = new ChannelService("channelId", "service", Service.Status.ACTIVE,
                LocalDateTime.now(), "userId");

        doReturn(0).when(jdbcTemplate).update(eq(CHANNEL_SERVICE_INSERT_SQL),
                (ChannelServiceInsertPreparedStatementSetter) notNull());

        assertThrows(IncorrectUpdateSemanticsDataAccessException.class,
                () -> channelServiceDao.insertChannelService(channelService));

        verify(jdbcTemplate, times(1)).update(eq(CHANNEL_SERVICE_INSERT_SQL),
                (ChannelServiceInsertPreparedStatementSetter) notNull());
    }

    @Test
    void updateChannelServiceStatusWithUserId() {
        ChannelService channelService = new ChannelService("channelId", "service", Service.Status.ACTIVE,
                LocalDateTime.now(), "userId");

        doReturn(1).when(jdbcTemplate).update((ChannelServiceUpdatePreparedStatementCreator) notNull());

        assertTrue(channelServiceDao.updateChannelServiceStatus(channelService, Service.Status.INACTIVE,
                "updatingUser"));

        ArgumentCaptor<ChannelServiceUpdatePreparedStatementCreator> channelServiceUpdatePreparedStatementCreatorCaptor =
                ArgumentCaptor.forClass(ChannelServiceUpdatePreparedStatementCreator.class);
        verify(jdbcTemplate, times(1)).update(channelServiceUpdatePreparedStatementCreatorCaptor.capture());

        ChannelServiceUpdatePreparedStatementCreator channelServiceUpdatePreparedStatementCreator =
                channelServiceUpdatePreparedStatementCreatorCaptor.getValue();
        assertSame(channelService, channelServiceUpdatePreparedStatementCreator.getChannelService(),
                "The channel service to update does not match.");
        assertEquals(Service.Status.INACTIVE, channelServiceUpdatePreparedStatementCreator.getChannelServiceStatus(),
                "The new channel service status does not match.");
        assertEquals("updatingUser", channelServiceUpdatePreparedStatementCreator.getUpdatingUser(),
                "The updating user id does not match.");
    }

    @Test
    void updateChannelServiceStatusWithoutUpdatingUserId() {
        ChannelService channelService = new ChannelService("channelId", "service", Service.Status.ACTIVE,
                LocalDateTime.now(), "userId");

        doReturn(1).when(jdbcTemplate).update((ChannelServiceUpdatePreparedStatementCreator) notNull());

        assertTrue(channelServiceDao.updateChannelServiceStatus(channelService, Service.Status.DISABLED, null));

        ArgumentCaptor<ChannelServiceUpdatePreparedStatementCreator> channelServiceUpdatePreparedStatementCreatorCaptor =
                ArgumentCaptor.forClass(ChannelServiceUpdatePreparedStatementCreator.class);
        verify(jdbcTemplate, times(1)).update(channelServiceUpdatePreparedStatementCreatorCaptor.capture());

        ChannelServiceUpdatePreparedStatementCreator channelServiceUpdatePreparedStatementCreator =
                channelServiceUpdatePreparedStatementCreatorCaptor.getValue();
        assertSame(channelService, channelServiceUpdatePreparedStatementCreator.getChannelService(),
                "The channel service to update does not match.");
        assertEquals(Service.Status.DISABLED, channelServiceUpdatePreparedStatementCreator.getChannelServiceStatus(),
                "The new channel service status does not match.");
        assertNull(channelServiceUpdatePreparedStatementCreator.getUpdatingUser(),
                "The updating user id should be null.");
    }

    @Test
    void updateChannelServiceStatusFails() {
        ChannelService channelService = new ChannelService("channelId", "service", Service.Status.ACTIVE,
                LocalDateTime.now(), "userId");

        doReturn(0).when(jdbcTemplate).update((ChannelServiceUpdatePreparedStatementCreator) notNull());

        assertFalse(channelServiceDao.updateChannelServiceStatus(channelService, Service.Status.INACTIVE,
                "updatingUser"));
    }

    @Test
    void updateChannelServiceStatusAffectsTooManyRecords() {
        ChannelService channelService = new ChannelService("channelId", "service", Service.Status.ACTIVE,
                LocalDateTime.now(), "userId");

        doReturn(2).when(jdbcTemplate).update((ChannelServiceUpdatePreparedStatementCreator) notNull());

        assertThrows(IncorrectUpdateSemanticsDataAccessException.class,
                () -> channelServiceDao.updateChannelServiceStatus(channelService, Service.Status.INACTIVE,
                        "updatingUser"));
    }

}