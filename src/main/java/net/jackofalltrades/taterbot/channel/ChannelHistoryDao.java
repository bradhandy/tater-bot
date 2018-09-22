package net.jackofalltrades.taterbot.channel;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.IncorrectUpdateSemanticsDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
class ChannelHistoryDao {

    private final JdbcTemplate jdbcTemplate;

    @Autowired
    ChannelHistoryDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    void insertChannelHistory(ChannelHistory channelHistory) {
        int recordsInserted = jdbcTemplate.update(
                "insert into channel_history (channel_id, member, member_reason, begin_date, end_date) " +
                        "values (?, ?, ?, ?, ?)",
                new ChannelHistoryInsertPreparedStatementSetter(channelHistory));

        if (recordsInserted != 1) {
            throw new IncorrectUpdateSemanticsDataAccessException(
                    String.format("Expected to insert one channel history record, but inserted %d.", recordsInserted));
        }
    }

}
