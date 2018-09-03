package net.jackofalltrades.taterbot.service;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.notNull;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.IncorrectUpdateSemanticsDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import java.time.LocalDateTime;

@ExtendWith(MockitoExtension.class)
class ServiceDaoTest {

    private static final String UPDATE_SERVICE_STATUS_SQL =
            "update service set status = ?, status_date = ? where code = ? and status = ? and status_date = ?";

    @Mock
    private JdbcTemplate jdbcTemplate;

    private ServiceRowMapper serviceRowMapper;
    private ServiceDao serviceDao;

    @BeforeEach
    void setUpServiceDao() {
        serviceRowMapper = new ServiceRowMapper();
        serviceDao = new ServiceDao(jdbcTemplate, serviceRowMapper);
    }

    @Test
    void retrieveServiceByCode() {
        Service databaseService = new Service("database", "description", Service.Status.ENABLED,
                LocalDateTime.now(), Service.Status.INACTIVE);

        doReturn(databaseService).when(jdbcTemplate)
                .queryForObject("select * from service where code = ?", serviceRowMapper, "database");

        assertSame(databaseService, serviceDao.findService("database"), "The database service could not be found.");
    }

    @Test
    void updateServiceStatusSucceeds() {
        Service databaseService = new Service("database", "description", Service.Status.DISABLED,
                LocalDateTime.now(), Service.Status.INACTIVE);

        doReturn(1).when(jdbcTemplate)
                .update(eq(UPDATE_SERVICE_STATUS_SQL), (ServiceStatusUpdatePreparedStatementSetter) notNull());

        assertTrue(serviceDao.updateServiceStatus(databaseService, Service.Status.ENABLED, LocalDateTime.now()));

        verify(jdbcTemplate, times(1)).update(eq(UPDATE_SERVICE_STATUS_SQL),
                (ServiceStatusUpdatePreparedStatementSetter) notNull());
    }

    @Test
    void updateServiceStatusFails() {
        Service databaseService = new Service("database", "description", Service.Status.DISABLED,
                LocalDateTime.now(), Service.Status.INACTIVE);

        doReturn(0).when(jdbcTemplate)
                .update(eq(UPDATE_SERVICE_STATUS_SQL), (ServiceStatusUpdatePreparedStatementSetter) notNull());

        assertFalse(serviceDao.updateServiceStatus(databaseService, Service.Status.ENABLED, LocalDateTime.now()));

        verify(jdbcTemplate, times(1)).update(eq(UPDATE_SERVICE_STATUS_SQL),
                (ServiceStatusUpdatePreparedStatementSetter) notNull());
    }

    @Test
    void updateServiceStatusFailsWhenUpdatingTooManyRecords() {
        Service databaseService = new Service("database", "description", Service.Status.DISABLED,
                LocalDateTime.now(), Service.Status.INACTIVE);

        doReturn(2).when(jdbcTemplate)
                .update(eq(UPDATE_SERVICE_STATUS_SQL), (ServiceStatusUpdatePreparedStatementSetter) notNull());

        assertThrows(IncorrectUpdateSemanticsDataAccessException.class,
                () -> serviceDao.updateServiceStatus(databaseService, Service.Status.ENABLED, LocalDateTime.now()),
                "The update should have failed when updating too many records.");
        verify(jdbcTemplate, times(1)).update(eq(UPDATE_SERVICE_STATUS_SQL),
                (ServiceStatusUpdatePreparedStatementSetter) notNull());
    }

}
