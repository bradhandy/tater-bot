package net.jackofalltrades.taterbot.channel;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.IncorrectUpdateSemanticsDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;

@Repository
class ChannelDao {

    private final JdbcTemplate jdbcTemplate;
    private final ChannelRowMapper channelRowMapper;

    @Autowired
    ChannelDao(JdbcTemplate jdbcTemplate, ChannelRowMapper channelRowMapper) {
        this.jdbcTemplate = jdbcTemplate;
        this.channelRowMapper = channelRowMapper;
    }

    Channel findChannel(String channelId) {
        return jdbcTemplate.queryForObject("select * from channel where channel_id = ?", channelRowMapper, channelId);
    }

    void insertChannel(Channel channel) {
        int recordsInserted = jdbcTemplate.update(
                "insert into channel (channel_id, member, member_reason, begin_date) values (?, ?, ?, ?)",
                new ChannelInsertPreparedStatementSetter(channel));
        if (recordsInserted != 1) {
            throw new IncorrectUpdateSemanticsDataAccessException(
                    String.format("Should have inserted 1 channel, but inserted %d.", recordsInserted));
        }
    }

    boolean updateChannel(Channel existingChannel, boolean member, String memberReason, LocalDateTime membershipTime) {
        int recordsUpdated = jdbcTemplate.update(
                "update channel set member = ?, member_reason = ?, membership_date = ? " +
                        "where channel_id = ? and member = ? and member_reason = ? and membership_date = ?",
                new ChannelUpdatePreparedStatementSetter(existingChannel, member, memberReason, membershipTime));
        if (recordsUpdated > 1) {
            throw new IncorrectUpdateSemanticsDataAccessException(
                    String.format("Should have updated one channel, but updated %d.", recordsUpdated));
        }

        return recordsUpdated == 1;
    }

}
