package net.jackofalltrades.taterbot.channel;

import static org.junit.jupiter.api.Assertions.assertEquals;
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
class ChannelHistoryDaoTest {

    @Mock
    private JdbcTemplate jdbcTemplate;

    private ChannelHistoryDao channelHistoryDao;

    @BeforeEach
    void setUpChannelHistoryDao() {
        channelHistoryDao = new ChannelHistoryDao(jdbcTemplate);
    }

    @Test
    void insertChannelHistorySucceeds() {
        ChannelHistory channelHistory =
                new ChannelHistory("channelId", true, "Invited", LocalDateTime.now().minus(5, ChronoUnit.HOURS),
                        LocalDateTime.now());

        doReturn(1)
                .when(jdbcTemplate)
                .update(contains("into channel_history "), (ChannelHistoryInsertPreparedStatementSetter) notNull());

        channelHistoryDao.insertChannelHistory(channelHistory);

        ArgumentCaptor<ChannelHistoryInsertPreparedStatementSetter> channelHistoryInsertPreparedStatementSetterCaptor =
                ArgumentCaptor.forClass(ChannelHistoryInsertPreparedStatementSetter.class);
        verify(jdbcTemplate, times(1))
                .update(contains("into channel_history "), channelHistoryInsertPreparedStatementSetterCaptor.capture());

        assertEquals(new ChannelHistoryInsertPreparedStatementSetter(channelHistory),
                channelHistoryInsertPreparedStatementSetterCaptor.getValue(),
                "The channel history prepared statement setter does not match.");
    }

    @Test
    void insertChannelHistoryFails() {
        ChannelHistory channelHistory = new ChannelHistory("channelId", false, "Kicked",
                LocalDateTime.now().minus(5, ChronoUnit.MINUTES), LocalDateTime.now());

        doReturn(0)
                .when(jdbcTemplate)
                .update(contains("into channel_history "), (ChannelHistoryInsertPreparedStatementSetter) notNull());

        assertThrows(IncorrectUpdateSemanticsDataAccessException.class,
                () -> channelHistoryDao.insertChannelHistory(channelHistory));
    }

}
