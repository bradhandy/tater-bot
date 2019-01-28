package net.jackofalltrades.taterbot.channel.record;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
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
import org.springframework.jdbc.core.PreparedStatementCreator;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@ExtendWith(MockitoExtension.class)
class ChannelRecordDaoTest {

    @Mock
    private JdbcTemplate jdbcTemplate;

    private ChannelRecordRowMapper channelRecordRowMapper;
    private ChannelRecordDao channelRecordDao;

    @BeforeEach
    void setUpChannelRecordDao() {
        channelRecordDao = new ChannelRecordDao(jdbcTemplate, channelRecordRowMapper);
    }

    @Test
    void channelRecordCanBeInsertedWithUserInformation() {
        LocalDateTime messageTimestamp = LocalDateTime.now();
        ChannelRecord channelRecord = new ChannelRecord("channelId", "userId", "displayName", "messageType",
                messageTimestamp, "message");

        doReturn(1).when(jdbcTemplate).update((PreparedStatementCreator) notNull());

        channelRecordDao.insertChannelRecord(channelRecord);

        ArgumentCaptor<ChannelRecordInsertPreparedStatementCreator> channelRecordInsertPreparedStatementCreatorCaptor =
                ArgumentCaptor.forClass(ChannelRecordInsertPreparedStatementCreator.class);
        verify(jdbcTemplate, times(1)).update(channelRecordInsertPreparedStatementCreatorCaptor.capture());

        ChannelRecordInsertPreparedStatementCreator expectedPreparedStatementCreator =
                new ChannelRecordInsertPreparedStatementCreator(channelRecord);
        assertEquals(expectedPreparedStatementCreator, channelRecordInsertPreparedStatementCreatorCaptor.getValue(),
                "The prepared statement creator does not match.");
    }

    @Test
    void channelRecordCanBeInsertedWithoutUserInformation() {
        LocalDateTime messageTimestamp = LocalDateTime.now();
        ChannelRecord channelRecord = new ChannelRecord("channelId", null, null, "messageType",
                messageTimestamp, "message");

        doReturn(1).when(jdbcTemplate).update((PreparedStatementCreator) notNull());

        channelRecordDao.insertChannelRecord(channelRecord);

        ArgumentCaptor<ChannelRecordInsertPreparedStatementCreator> channelRecordInsertPreparedStatementCreatorCaptor =
                ArgumentCaptor.forClass(ChannelRecordInsertPreparedStatementCreator.class);
        verify(jdbcTemplate, times(1)).update(channelRecordInsertPreparedStatementCreatorCaptor.capture());

        ChannelRecordInsertPreparedStatementCreator expectedPreparedStatementCreator =
                new ChannelRecordInsertPreparedStatementCreator(channelRecord);
        assertEquals(expectedPreparedStatementCreator, channelRecordInsertPreparedStatementCreatorCaptor.getValue(),
                "The prepared statement creator does not match.");
    }

    @Test
    void channelRecordFailsToInsert() {
        LocalDateTime messageTimestamp = LocalDateTime.now();
        ChannelRecord channelRecord = new ChannelRecord("channelId", null, null, "messageType", messageTimestamp,
                "message");

        doReturn(0).when(jdbcTemplate).update((PreparedStatementCreator) notNull());

        assertThrows(IncorrectUpdateSemanticsDataAccessException.class,
                () -> channelRecordDao.insertChannelRecord(channelRecord));
    }

    @Test
    void handleChannelRecordsAsTheyAreProcessedFromTheResultSet(@Mock ChannelRecordProcessor channelRecordProcessor) {
        LocalDateTime beginTimestamp = LocalDateTime.now().minus(5, ChronoUnit.MINUTES);
        LocalDateTime endTimestamp = LocalDateTime.now();

        channelRecordDao.processChannelRecords("channelId", beginTimestamp, endTimestamp, channelRecordProcessor);

        ArgumentCaptor<ChannelRecordProcessingRowCallbackHandler> channelRecordProcessingRowCallbackHandlerCaptor =
                ArgumentCaptor.forClass(ChannelRecordProcessingRowCallbackHandler.class);
        verify(jdbcTemplate, times(1)).query(contains("from channel_record"),
                channelRecordProcessingRowCallbackHandlerCaptor.capture(), eq("channelId"),
                eq(Timestamp.valueOf(beginTimestamp)), eq(Timestamp.valueOf(endTimestamp)));

        assertEquals(new ChannelRecordProcessingRowCallbackHandler(channelRecordRowMapper, channelRecordProcessor),
                channelRecordProcessingRowCallbackHandlerCaptor.getValue(),
                "The row callback handler does not match.");
    }

}
