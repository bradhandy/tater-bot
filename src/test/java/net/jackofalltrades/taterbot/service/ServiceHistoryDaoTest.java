package net.jackofalltrades.taterbot.service;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.notNull;
import static org.mockito.Mockito.doReturn;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.IncorrectUpdateSemanticsDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@ExtendWith(MockitoExtension.class)
class ServiceHistoryDaoTest {

    public static final String SERVICE_HISTORY_INSERT_STATEMENT =
            "insert into service_history (code, description, status, initial_channel_status, begin_date, end_date) " +
                    "values (?, ?, ?, ?, ?, ?)";
    @Mock
    private JdbcTemplate jdbcTemplate;

    private ServiceHistoryDao serviceHistoryDao;
    private ServiceHistory serviceHistory;

    @BeforeEach
    void setUpServiceHistoryDao() {
        serviceHistoryDao = new ServiceHistoryDao(jdbcTemplate);
    }

    @BeforeEach
    void setUpServiceHistory() {
        serviceHistory = new ServiceHistory("code", "description", Service.Status.INACTIVE, Service.Status.ACTIVE,
                LocalDateTime.now().minus(5, ChronoUnit.MINUTES), LocalDateTime.now());
    }

    @Test
    void insertServiceHistorySucceeds() {
        doReturn(1).when(jdbcTemplate).update(eq(SERVICE_HISTORY_INSERT_STATEMENT),
                (ServiceHistoryInsertPreparedStatementSetter) notNull());

        serviceHistoryDao.insertServiceHistory(serviceHistory);
    }

    @Test
    void insertServiceHistoryFails() {
        doReturn(0).when(jdbcTemplate).update(eq(SERVICE_HISTORY_INSERT_STATEMENT),
                (ServiceStatusUpdatePreparedStatementSetter) notNull());

        assertThrows(IncorrectUpdateSemanticsDataAccessException.class,
                () -> serviceHistoryDao.insertServiceHistory(serviceHistory),
                "Service history insert should have failed.");
    }

}
