package net.jackofalltrades.taterbot.channel.record;

import org.springframework.dao.IncorrectUpdateSemanticsDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import java.sql.Timestamp;
import java.time.LocalDateTime;

@Component
class ChannelRecordDao {

    private final JdbcTemplate jdbcTemplate;
    private final ChannelRecordRowMapper channelRecordRowMapper;

    public ChannelRecordDao(JdbcTemplate jdbcTemplate, ChannelRecordRowMapper channelRecordRowMapper) {
        this.jdbcTemplate = jdbcTemplate;
        this.channelRecordRowMapper = channelRecordRowMapper;
    }

    void insertChannelRecord(ChannelRecord channelRecord) {
        int channelRecordsInserted =
                jdbcTemplate.update(new ChannelRecordInsertPreparedStatementCreator(channelRecord));

        if (channelRecordsInserted != 1) {
            throw new IncorrectUpdateSemanticsDataAccessException(
                    String.format("Should have inserted 1 channel record, but inserted %d", channelRecordsInserted));
        }
    }

    void processChannelRecords(String channelId, LocalDateTime beginTimestamp, LocalDateTime endTimestamp,
            ChannelRecordProcessor channelRecordProcessor) {
        jdbcTemplate.query(
                "select * from channel_record where channel_id = ? and ? <= message_timestamp and message_timestamp <= ?",
                new ChannelRecordProcessingRowCallbackHandler(channelRecordRowMapper, channelRecordProcessor),
                channelId, Timestamp.valueOf(beginTimestamp), Timestamp.valueOf(endTimestamp));
    }

}
