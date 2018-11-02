package net.jackofalltrades.taterbot.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.AdditionalMatchers.and;
import static org.mockito.AdditionalMatchers.not;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.notNull;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.google.common.collect.Lists;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.IncorrectUpdateSemanticsDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

@ExtendWith(MockitoExtension.class)
class ChannelServiceDaoTest {

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
        ChannelService databaseChannelService =
                new ChannelService("channelId", "service", Service.Status.ACTIVE, LocalDateTime.now(), "userId");

        doReturn(databaseChannelService).when(jdbcTemplate)
                .queryForObject(eq("select * from channel_service where channel_id = ? and service_code = ?"),
                        (ChannelServiceRowMapper) notNull(), eq("channelId"), eq("service"));

        ChannelService channelService = channelServiceDao.findChannelService(channelServiceKey);
        assertSame(databaseChannelService, channelService, "The channel service did not match.");
    }

    @Test
    void insertNewChannelService() {
        ChannelService channelService =
                new ChannelService("channelId", "service", Service.Status.ACTIVE, LocalDateTime.now(), "userId");

        doReturn(1).when(jdbcTemplate)
                .update(and(contains("insert into channel_service"), contains("values (?, ?, ?, ?, ?)")),
                        (ChannelServiceInsertPreparedStatementSetter) notNull());

        channelServiceDao.insertChannelService(channelService);

        ArgumentCaptor<ChannelServiceInsertPreparedStatementSetter> channelServiceInsertPreparedStatementSetterCaptor =
                ArgumentCaptor.forClass(ChannelServiceInsertPreparedStatementSetter.class);
        verify(jdbcTemplate, times(1))
                .update(and(contains("insert into channel_service"), contains("values (?, ?, ?, ?, ?)")),
                        channelServiceInsertPreparedStatementSetterCaptor.capture());

        assertSame(channelService, channelServiceInsertPreparedStatementSetterCaptor.getValue().getChannelService(),
                "The channel service inserted does not match.");
    }

    @Test
    void insertNewChannelServiceFails() {
        ChannelService channelService =
                new ChannelService("channelId", "service", Service.Status.ACTIVE, LocalDateTime.now(), "userId");

        doReturn(0).when(jdbcTemplate)
                .update(and(contains("insert into channel_service"), contains("values (?, ?, ?, ?, ?)")),
                        (ChannelServiceInsertPreparedStatementSetter) notNull());

        assertThrows(IncorrectUpdateSemanticsDataAccessException.class,
                () -> channelServiceDao.insertChannelService(channelService));

        verify(jdbcTemplate, times(1))
                .update(and(contains("insert into channel_service"), contains("values (?, ?, ?, ?, ?)")),
                        (ChannelServiceInsertPreparedStatementSetter) notNull());
    }

    @Test
    void updateChannelServiceStatusWithUserId() {
        ChannelService channelService =
                new ChannelService("channelId", "service", Service.Status.ACTIVE, LocalDateTime.now(), "userId");

        doReturn(1).when(jdbcTemplate).update((ChannelServiceUpdatePreparedStatementCreator) notNull());

        assertTrue(channelServiceDao.updateChannelServiceStatus(channelService, Service.Status.INACTIVE,
                        LocalDateTime.now(), "updatingUser"));

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
                LocalDateTime.now().minus(5, ChronoUnit.HOURS), "userId");

        doReturn(1).when(jdbcTemplate).update((ChannelServiceUpdatePreparedStatementCreator) notNull());

        LocalDateTime channelServiceStatusDate = LocalDateTime.now();
        assertTrue(channelServiceDao.updateChannelServiceStatus(channelService, Service.Status.DISABLED,
                channelServiceStatusDate, null));

        ArgumentCaptor<ChannelServiceUpdatePreparedStatementCreator>
                channelServiceUpdatePreparedStatementCreatorCaptor =
                ArgumentCaptor.forClass(ChannelServiceUpdatePreparedStatementCreator.class);
        verify(jdbcTemplate, times(1)).update(channelServiceUpdatePreparedStatementCreatorCaptor.capture());

        ChannelServiceUpdatePreparedStatementCreator channelServiceUpdatePreparedStatementCreator =
                channelServiceUpdatePreparedStatementCreatorCaptor.getValue();
        assertSame(channelService, channelServiceUpdatePreparedStatementCreator.getChannelService(),
                "The channel service to update does not match.");
        assertEquals(Service.Status.DISABLED, channelServiceUpdatePreparedStatementCreator.getChannelServiceStatus(),
                "The new channel service status does not match.");
        assertEquals(channelServiceStatusDate,
                channelServiceUpdatePreparedStatementCreator.getChannelServiceStatusDate(),
                "The new channel service status date does not match.");
        assertNull(channelServiceUpdatePreparedStatementCreator.getUpdatingUser(),
                "The updating user id should be null.");
    }

    @Test
    void updateChannelServiceStatusFails() {
        ChannelService channelService =
                new ChannelService("channelId", "service", Service.Status.ACTIVE, LocalDateTime.now(), "userId");

        doReturn(0).when(jdbcTemplate).update((ChannelServiceUpdatePreparedStatementCreator) notNull());

        assertFalse(channelServiceDao.updateChannelServiceStatus(channelService, Service.Status.INACTIVE,
                        LocalDateTime.now(), "updatingUser"));
    }

    @Test
    void updateChannelServiceStatusAffectsTooManyRecords() {
        ChannelService channelService =
                new ChannelService("channelId", "service", Service.Status.ACTIVE, LocalDateTime.now(), "userId");

        doReturn(2).when(jdbcTemplate).update((ChannelServiceUpdatePreparedStatementCreator) notNull());

        assertThrows(IncorrectUpdateSemanticsDataAccessException.class, () -> channelServiceDao
                .updateChannelServiceStatus(channelService, Service.Status.INACTIVE, LocalDateTime.now(), "updatingUser"));
    }

    @Test
    void findMissingServices() {
        ArrayList<String> expectedMissingServicesList = Lists.newArrayList("service", "missing");

        doReturn(expectedMissingServicesList).when(jdbcTemplate)
                .query(and(contains("select code"), contains("service left outer join")),
                        (StringColumnListResultSetExtractor) notNull(), eq("channelId"));

        assertSame(expectedMissingServicesList, channelServiceDao.findMissingServicesForChannel("channelId"));
    }

    @Test
    void findAllAvailableServices() {
        List<ChannelService> expectedChannelServices = Lists.newArrayList(
                new ChannelService("channelId", "service", Service.Status.ACTIVE, LocalDateTime.now(), "userId"),
                new ChannelService("channelId", "service2", Service.Status.INACTIVE, LocalDateTime.now(), null));

        doReturn(expectedChannelServices)
                .when(jdbcTemplate)
                .query(and(contains("from channel"), not(contains("service_code = ?"))),
                        (ChannelServiceRowMapper) notNull(), eq("channelId"));

        List<ChannelService> channelServices = channelServiceDao.retrieveChannelServices("channelId");

        assertSame(expectedChannelServices, channelServices, "The list of ChannelService instances does not match.");
    }

}
