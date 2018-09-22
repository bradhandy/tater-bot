package net.jackofalltrades.taterbot.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.IncorrectUpdateSemanticsDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
class ChannelServiceHistoryDao {

    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public ChannelServiceHistoryDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    void insertChannelServiceHistory(ChannelServiceHistory channelServiceHistory) {
        int recordsInserted = jdbcTemplate.update(
                "insert into channel_service_history (channel_id, service_code, status, begin_date, end_date, user_id) "
                        + "values (?, ?, ?, ?, ?, ?)",
                new ChannelServiceHistoryInsertPreparedStatementSetter(channelServiceHistory));

        if (recordsInserted != 1) {
            throw new IncorrectUpdateSemanticsDataAccessException(
                    String.format("Should have inserted 1 channel service history, but inserted %d.", recordsInserted));
        }
    }

}
