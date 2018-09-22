package net.jackofalltrades.taterbot.service;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.contains;
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
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@ExtendWith(MockitoExtension.class)
class ChannelServiceHistoryDaoTest {

    @Mock
    private JdbcTemplate jdbcTemplate;

    private ChannelServiceHistoryDao channelServiceHistoryDao;
    private ChannelServiceHistory channelServiceHistory;

    @BeforeEach
    void setUpChannelServiceHistoryDao() {
        channelServiceHistoryDao = new ChannelServiceHistoryDao(jdbcTemplate);
    }

    @BeforeEach
    void setUpChannelServiceHistory() {
        channelServiceHistory = new ChannelServiceHistory("channelId", "service", Service.Status.ACTIVE,
                LocalDateTime.now().minus(5, ChronoUnit.DAYS), LocalDateTime.now(), "userId");
    }

    @Test
    void insertChannelServiceHistorySucceeds() {
        doReturn(1).when(jdbcTemplate).update(contains("insert into channel_service_history"),
                (ChannelServiceHistoryInsertPreparedStatementSetter) notNull());

        channelServiceHistoryDao.insertChannelServiceHistory(channelServiceHistory);

        ArgumentCaptor<ChannelServiceHistoryInsertPreparedStatementSetter> channelServiceHistoryPreparedStatementSetterCaptor =
                ArgumentCaptor.forClass(ChannelServiceHistoryInsertPreparedStatementSetter.class);
        verify(jdbcTemplate, times(1)).update(contains("insert into channel_service_history"),
                channelServiceHistoryPreparedStatementSetterCaptor.capture());

        ChannelServiceHistoryInsertPreparedStatementSetter channelServiceHistoryInsertPreparedStatementSetter =
                channelServiceHistoryPreparedStatementSetterCaptor.getValue();
        assertSame(channelServiceHistory, channelServiceHistoryInsertPreparedStatementSetter.getChannelServiceHistory(),
                "THe channel service history does not match.");
    }

    @Test
    void insertChannelServiceHistoryFails() {
        doReturn(0).when(jdbcTemplate).update(contains("insert into channel_service_history"),
                (ChannelServiceHistoryInsertPreparedStatementSetter) notNull());

        assertThrows(IncorrectUpdateSemanticsDataAccessException.class,
                () -> channelServiceHistoryDao.insertChannelServiceHistory(channelServiceHistory));
    }

}
