package net.jackofalltrades.taterbot.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.IncorrectUpdateSemanticsDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
class ChannelServiceDao {

    private final JdbcTemplate jdbcTemplate;
    private final ChannelServiceRowMapper channelServiceRowMapper;

    @Autowired
    ChannelServiceDao(JdbcTemplate jdbcTemplate, ChannelServiceRowMapper channelServiceRowMapper) {
        this.jdbcTemplate = jdbcTemplate;
        this.channelServiceRowMapper = channelServiceRowMapper;
    }

    ChannelService findChannelService(ChannelServiceKey channelServiceKey) {
        return jdbcTemplate.queryForObject("select * from channel_service where channel_id = ? and service_code = ?",
                channelServiceRowMapper, channelServiceKey.getChannelId(), channelServiceKey.getServiceCode());
    }

    void insertChannelService(ChannelService channelService) {
        int recordsInserted = jdbcTemplate.update(
                "insert into channel_service (channel_id, service_code, status, status_date, user_id) " +
                        "values (?, ?, ?, ?, ?)",
                new ChannelServiceInsertPreparedStatementSetter(channelService));

        if (recordsInserted != 1) {
            throw new IncorrectUpdateSemanticsDataAccessException(
                    String.format("Expected to insert 1 channe service record, but inserted %d.", recordsInserted));
        }
    }

    boolean updateChannelServiceStatus(ChannelService channelService, Service.Status channelServiceStatus,
            String updatingUser) {
        int recordsUpdated = jdbcTemplate.update(new ChannelServiceUpdatePreparedStatementCreator(channelService,
                channelServiceStatus, updatingUser));
        if (recordsUpdated > 1) {
            throw new IncorrectUpdateSemanticsDataAccessException(
                    String.format("Expected to update 1 channel service record, but updated %d records.",
                            recordsUpdated));
        }

        return recordsUpdated == 1;
    }

}
